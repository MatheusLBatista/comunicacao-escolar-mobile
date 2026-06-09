package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import dev.fslab.comunicacao.escolar.model.CreateEventRequest
import dev.fslab.comunicacao.escolar.model.CreateEventTarget
import dev.fslab.comunicacao.escolar.model.Evento
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.model.UpdateEventRequest
import dev.fslab.comunicacao.escolar.model.toEvento
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AgendaViewModel : ViewModel() {

    companion object {
        private const val TAG = "AgendaViewModel"
    }

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mesAtual = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val mesAtual: StateFlow<LocalDate> = _mesAtual.asStateFlow()

    private val _diaSelecionado = MutableStateFlow<LocalDate?>(LocalDate.now())
    val diaSelecionado: StateFlow<LocalDate?> = _diaSelecionado.asStateFlow()

    private val _turmas = MutableStateFlow<List<Turma>>(emptyList())
    val turmas: StateFlow<List<Turma>> = _turmas.asStateFlow()

    private val _criandoEvento = MutableStateFlow(false)
    val criandoEvento: StateFlow<Boolean> = _criandoEvento.asStateFlow()

    private val _erroCreate = MutableStateFlow<String?>(null)
    val erroCreate: StateFlow<String?> = _erroCreate.asStateFlow()

    private val _editandoEvento = MutableStateFlow(false)
    val editandoEvento: StateFlow<Boolean> = _editandoEvento.asStateFlow()

    private val _erroEdit = MutableStateFlow<String?>(null)
    val erroEdit: StateFlow<String?> = _erroEdit.asStateFlow()

    private val _excluindoEvento = MutableStateFlow(false)
    val excluindoEvento: StateFlow<Boolean> = _excluindoEvento.asStateFlow()

    private val _erroExcluir = MutableStateFlow<String?>(null)
    val erroExcluir: StateFlow<String?> = _erroExcluir.asStateFlow()

    private var schoolIdAtual: String = ""

    fun carregarEventos(schoolId: String, mes: LocalDate = _mesAtual.value) {
        if (schoolId.isBlank()) return
        schoolIdAtual = schoolId
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.agendaApi.listEvents(
                    mapOf(
                        "month" to mes.monthValue.toString(),
                        "year" to mes.year.toString(),
                        "limit" to "200"
                    )
                )
                if (!response.error) {
                    _eventos.value = response.data?.docs?.map { it.toEvento() } ?: emptyList()
                } else {
                    _error.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Erro ao carregar eventos"
                Log.e(TAG, "carregarEventos error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun carregarTurmas(schoolId: String) {
        if (schoolId.isBlank() || _turmas.value.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.listClasses(schoolId, mapOf("limit" to "100"))
                if (!response.error) {
                    _turmas.value = response.data?.docs?.map { it.toTurma() } ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "carregarTurmas error", e)
            }
        }
    }

    fun criarEvento(
        titulo: String,
        descricao: String?,
        inicio: String,
        fim: String?,
        turmasSelecionadas: List<String>,
        allDay: Boolean = false,
        onSuccess: () -> Unit
    ) {
        Log.d(TAG, "criarEvento: schoolId='$schoolIdAtual' titulo='$titulo' inicio='$inicio'")
        if (schoolIdAtual.isBlank()) {
            _erroCreate.value = "Escola não identificada. Tente sair e entrar novamente."
            return
        }
        _criandoEvento.value = true
        _erroCreate.value = null
        viewModelScope.launch {
            try {
                val target = if (turmasSelecionadas.isEmpty()) {
                    CreateEventTarget(scope = "all", targetIds = emptyList())
                } else {
                    CreateEventTarget(scope = "class", targetIds = turmasSelecionadas)
                }
                val request = CreateEventRequest(
                    schoolId = schoolIdAtual,
                    title = titulo,
                    description = descricao?.takeIf { it.isNotBlank() },
                    type = "event",
                    startDate = inicio,
                    endDate = fim?.takeIf { it.isNotBlank() },
                    allDay = allDay,
                    target = target
                )
                Log.d(TAG, "criarEvento: enviando POST /events startDate=$inicio")
                val response = RetrofitClient.agendaApi.createEvent(request)
                Log.d(TAG, "criarEvento: resposta error=${response.error} id=${response.data?.id}")
                if (!response.error) {
                    Log.d(TAG, "criarEvento: SUCCESS — recarregando eventos do mês ${_mesAtual.value}")
                    val diaEvento = runCatching { LocalDate.parse(inicio.take(10)) }.getOrNull()
                    if (diaEvento != null) {
                        _mesAtual.value = diaEvento.withDayOfMonth(1)
                        _diaSelecionado.value = diaEvento
                    }
                    recarregarEventos()
                    onSuccess()
                } else {
                    _erroCreate.value = response.getErrorMessage().ifBlank { "Erro ao criar evento. Tente novamente." }
                }
            } catch (e: HttpException) {
                val msg = runCatching {
                    val body = e.response()?.errorBody()?.string() ?: ""
                    Gson().fromJson(body, JsonObject::class.java)?.get("message")?.asString
                }.getOrNull()
                _erroCreate.value = msg ?: "Erro ${e.code()}: ${e.message()}"
                Log.e(TAG, "criarEvento HTTP error ${e.code()}", e)
            } catch (e: Exception) {
                _erroCreate.value = e.localizedMessage ?: "Erro ao criar evento"
                Log.e(TAG, "criarEvento error", e)
            } finally {
                _criandoEvento.value = false
            }
        }
    }

    fun limparErroCreate() {
        _erroCreate.value = null
    }

    fun editarEvento(
        id: String,
        titulo: String,
        descricao: String?,
        inicio: String,
        fim: String?,
        turmasSelecionadas: List<String>,
        allDay: Boolean,
        onSuccess: () -> Unit
    ) {
        _editandoEvento.value = true
        _erroEdit.value = null
        viewModelScope.launch {
            try {
                val target = if (turmasSelecionadas.isEmpty()) {
                    CreateEventTarget(scope = "all", targetIds = emptyList())
                } else {
                    CreateEventTarget(scope = "class", targetIds = turmasSelecionadas)
                }
                val request = UpdateEventRequest(
                    title = titulo,
                    description = descricao?.takeIf { it.isNotBlank() },
                    startDate = inicio,
                    endDate = fim?.takeIf { it.isNotBlank() },
                    allDay = allDay,
                    target = target
                )
                val response = RetrofitClient.agendaApi.updateEvent(id, request)
                if (!response.error) {
                    recarregarEventos()
                    onSuccess()
                } else {
                    _erroEdit.value = response.getErrorMessage().ifBlank { "Erro ao editar evento." }
                }
            } catch (e: HttpException) {
                val msg = runCatching {
                    val body = e.response()?.errorBody()?.string() ?: ""
                    Gson().fromJson(body, JsonObject::class.java)?.get("message")?.asString
                }.getOrNull()
                _erroEdit.value = msg ?: "Erro ${e.code()}: ${e.message()}"
                Log.e(TAG, "editarEvento HTTP error ${e.code()}", e)
            } catch (e: Exception) {
                _erroEdit.value = e.localizedMessage ?: "Erro ao editar evento"
                Log.e(TAG, "editarEvento error", e)
            } finally {
                _editandoEvento.value = false
            }
        }
    }

    fun excluirEvento(id: String, onSuccess: () -> Unit) {
        _excluindoEvento.value = true
        _erroExcluir.value = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.agendaApi.deleteEvent(id)
                if (!response.error) {
                    recarregarEventos()
                    onSuccess()
                } else {
                    _erroExcluir.value = response.getErrorMessage().ifBlank { "Erro ao excluir evento." }
                }
            } catch (e: HttpException) {
                val msg = runCatching {
                    val body = e.response()?.errorBody()?.string() ?: ""
                    Gson().fromJson(body, JsonObject::class.java)?.get("message")?.asString
                }.getOrNull()
                _erroExcluir.value = msg ?: "Erro ${e.code()}: ${e.message()}"
                Log.e(TAG, "excluirEvento HTTP error ${e.code()}", e)
            } catch (e: Exception) {
                _erroExcluir.value = e.localizedMessage ?: "Erro ao excluir evento"
                Log.e(TAG, "excluirEvento error", e)
            } finally {
                _excluindoEvento.value = false
            }
        }
    }

    fun limparErroEdit() { _erroEdit.value = null }
    fun limparErroExcluir() { _erroExcluir.value = null }

    private suspend fun recarregarEventos() {
        _loading.value = true
        try {
            val response = RetrofitClient.agendaApi.listEvents(
                mapOf(
                    "month" to _mesAtual.value.monthValue.toString(),
                    "year" to _mesAtual.value.year.toString(),
                    "limit" to "200"
                )
            )
            if (!response.error) {
                _eventos.value = response.data?.docs?.map { it.toEvento() } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "recarregarEventos error", e)
        } finally {
            _loading.value = false
        }
    }

    fun irParaMesAnterior() {
        val novoMes = _mesAtual.value.minusMonths(1)
        _mesAtual.value = novoMes
        _diaSelecionado.value = null
        carregarEventos(schoolIdAtual, novoMes)
    }

    fun irParaProximoMes() {
        val novoMes = _mesAtual.value.plusMonths(1)
        _mesAtual.value = novoMes
        _diaSelecionado.value = null
        carregarEventos(schoolIdAtual, novoMes)
    }

    fun selecionarDia(dia: LocalDate) {
        _diaSelecionado.value = if (_diaSelecionado.value == dia) null else dia
    }

    fun eventosNoDia(dia: LocalDate): List<Evento> {
        return _eventos.value.filter { evento ->
            runCatching {
                val dataEvento = LocalDate.parse(evento.dataInicio.take(10))
                dataEvento == dia
            }.getOrDefault(false)
        }
    }

    fun diasComEventos(): Set<LocalDate> {
        return _eventos.value.mapNotNull { evento ->
            runCatching { LocalDate.parse(evento.dataInicio.take(10)) }.getOrNull()
        }.toSet()
    }

    fun formatarDataParaApi(data: LocalDate, hora: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return "${data.format(formatter)}T${hora}:00.000Z"
    }
}
