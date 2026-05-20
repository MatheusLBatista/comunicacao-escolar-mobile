package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.Evento
import dev.fslab.comunicacao.escolar.model.toEvento
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    private var schoolIdAtual: String = ""

    fun carregarEventos(schoolId: String, mes: LocalDate = _mesAtual.value) {
        if (schoolId.isBlank()) return
        schoolIdAtual = schoolId
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.agendaApi.listEvents(
                    schoolId,
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
}
