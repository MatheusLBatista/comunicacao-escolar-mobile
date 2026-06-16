package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudent
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaida
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.model.CreateAuthorizedPerson
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationRequest
import dev.fslab.comunicacao.escolar.model.CreatePickupLogRequest
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoRequest
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoUsedRequest
import dev.fslab.comunicacao.escolar.model.PickedUpBy
import dev.fslab.comunicacao.escolar.model.PickupLogUi
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

sealed class AutorizacaoSaidaUiState {
    object Loading : AutorizacaoSaidaUiState()
    data class Error(val message: String) : AutorizacaoSaidaUiState()
    object Empty : AutorizacaoSaidaUiState()
    data class Content(val autorizacoes: List<AutorizacaoSaida>) : AutorizacaoSaidaUiState()
}

sealed class AutorizacaoFiltro {
    object Ativas : AutorizacaoFiltro()
    object Canceladas : AutorizacaoFiltro()
    object Saidas : AutorizacaoFiltro()
}

sealed class PickupLogsUiState {
    object Loading : PickupLogsUiState()
    object Empty : PickupLogsUiState()
    data class Content(val logs: List<PickupLogUi>) : PickupLogsUiState()
    data class Error(val message: String) : PickupLogsUiState()
}

class AutorizacaoSaidaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AutorizacaoSaidaUiState>(AutorizacaoSaidaUiState.Loading)
    val uiState: StateFlow<AutorizacaoSaidaUiState> = _uiState.asStateFlow()

    private val _cancelando = MutableStateFlow<String?>(null)
    val cancelando: StateFlow<String?> = _cancelando.asStateFlow()

    private val _registrando = MutableStateFlow<String?>(null)
    val registrando: StateFlow<String?> = _registrando.asStateFlow()

    private val _revertendo = MutableStateFlow<String?>(null)
    val revertendo: StateFlow<String?> = _revertendo.asStateFlow()

    private val _showNovaAutorizacaoSheet = MutableStateFlow(false)
    val showNovaAutorizacaoSheet: StateFlow<Boolean> = _showNovaAutorizacaoSheet.asStateFlow()

    private val _criando = MutableStateFlow(false)
    val criando: StateFlow<Boolean> = _criando.asStateFlow()

    private val _criarErro = MutableStateFlow<String?>(null)
    val criarErro: StateFlow<String?> = _criarErro.asStateFlow()

    private val _rawDocs = MutableStateFlow<List<AutorizacaoSaidaDoc>>(emptyList())

    private val _alunos = MutableStateFlow<List<ApiAssociatedStudent>>(emptyList())
    val alunos: StateFlow<List<ApiAssociatedStudent>> = _alunos.asStateFlow()

    private val _filtro = MutableStateFlow<AutorizacaoFiltro>(AutorizacaoFiltro.Ativas)
    val filtro: StateFlow<AutorizacaoFiltro> = _filtro.asStateFlow()

    private val _pickupLogsState = MutableStateFlow<PickupLogsUiState>(PickupLogsUiState.Loading)
    val pickupLogsState: StateFlow<PickupLogsUiState> = _pickupLogsState.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    private var classNameById: Map<String, String> = emptyMap()

    init {
        loadAlunos()
        val user = TokenManager.getSavedUser()
        if (user?.role == "teacher") {
            viewModelScope.launch {
                runCatching {
                    val schoolId = user.schoolId ?: return@runCatching
                    val token = TokenManager.getAccessToken() ?: return@runCatching
                    val classes = RetrofitClient.adminApi.listClasses(
                        schoolId,
                        mapOf("teacher_id" to user.id, "limit" to "100")
                    ).data?.docs.orEmpty()
                    classNameById = classes.associate { it.id to it.name }
                }
                loadAutorizacoes()
            }
        } else {
            loadAutorizacoes()
        }
    }

    private fun loadAlunos() {
        val json = TokenManager.getStudentsJson()
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<ApiAssociatedStudent>>() {}.type
                _alunos.value = Gson().fromJson(json, type)
            } catch (_: Exception) {}
        }
    }

    fun setFiltro(filtro: AutorizacaoFiltro) {
        _filtro.value = filtro
        if (filtro is AutorizacaoFiltro.Saidas) loadPickupLogs() else loadAutorizacoes()
    }

    fun loadPickupLogs() {
        viewModelScope.launch {
            _pickupLogsState.value = PickupLogsUiState.Loading
            val token = TokenManager.getAccessToken() ?: return@launch
            try {
                val docs = RetrofitClient.autorizacaoSaidaApi.getPickupLogs(
                    "Bearer $token"
                ).data?.docs.orEmpty()
                _pickupLogsState.value = if (docs.isEmpty()) PickupLogsUiState.Empty
                else PickupLogsUiState.Content(docs.map { it.toPickupLogUi() })
            } catch (e: Exception) {
                _pickupLogsState.value = PickupLogsUiState.Error(e.message ?: "Erro ao carregar saídas.")
            }
        }
    }

    fun loadAutorizacoes() {
        viewModelScope.launch {
            _uiState.value = AutorizacaoSaidaUiState.Loading

            val token = TokenManager.getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = AutorizacaoSaidaUiState.Error("Sessão expirada. Faça login novamente.")
                return@launch
            }

            try {
                val isAtivas = _filtro.value is AutorizacaoFiltro.Ativas
                val active = if (_filtro.value is AutorizacaoFiltro.Canceladas) false else true
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacoes(
                    token = "Bearer $token",
                    active = active,
                    used = if (isAtivas) false else null
                )
                if (response.error) {
                    _uiState.value = AutorizacaoSaidaUiState.Error(response.getErrorMessage())
                    return@launch
                }
                val now = java.util.Date()
                val docs = response.data?.docs.orEmpty().let { all ->
                    if (isAtivas) all.filter { doc ->
                        runCatching { isoFormatter.parse(doc.validUntil)?.after(now) }.getOrNull() == true
                    } else all
                }
                _rawDocs.value = docs
                val list = docs.map { it.toUi() }
                _uiState.value = if (list.isEmpty()) AutorizacaoSaidaUiState.Empty else AutorizacaoSaidaUiState.Content(list)
            } catch (e: retrofit2.HttpException) {
                _uiState.value = AutorizacaoSaidaUiState.Error("Erro ao carregar autorizações (${e.code()}).")
            } catch (e: java.net.UnknownHostException) {
                _uiState.value = AutorizacaoSaidaUiState.Error("Sem conexão com a internet.")
            } catch (e: java.net.SocketTimeoutException) {
                _uiState.value = AutorizacaoSaidaUiState.Error("Tempo de conexão esgotado.")
            } catch (e: Exception) {
                _uiState.value = AutorizacaoSaidaUiState.Error("Erro ao carregar autorizações.")
            }
        }
    }

    fun cancelarAutorizacao(id: String) {
        viewModelScope.launch {
            _cancelando.value = id
            val token = TokenManager.getAccessToken() ?: run {
                _cancelando.value = null
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.cancelarAutorizacao("Bearer $token", id, PatchAutorizacaoRequest(active = false))
                if (response.isSuccessful) {
                    val updated = _rawDocs.value.filter { it.id != id }
                    _rawDocs.value = updated
                    val list = updated.filter { it.active }.map { it.toUi() }
                    _uiState.value = if (list.isEmpty()) AutorizacaoSaidaUiState.Empty
                                     else AutorizacaoSaidaUiState.Content(list)
                }
            } catch (_: Exception) {
            } finally {
                _cancelando.value = null
            }
        }
    }

    fun abrirNovaAutorizacao() {
        _criarErro.value = null
        _showNovaAutorizacaoSheet.value = true
    }

    fun fecharNovaAutorizacao() {
        _showNovaAutorizacaoSheet.value = false
    }

    fun criarAutorizacao(nome: String, documento: String, relacao: String, validFromMs: Long, validUntilMs: Long, studentId: String) {
        viewModelScope.launch {
            _criando.value = true
            _criarErro.value = null

            val token = TokenManager.getAccessToken() ?: run {
                _criarErro.value = "Sessão expirada."
                _criando.value = false
                return@launch
            }

            val savedUser = TokenManager.getSavedUser() ?: run {
                _criarErro.value = "Usuário não encontrado."
                _criando.value = false
                return@launch
            }

            val schoolId = savedUser.schoolId ?: run {
                _criarErro.value = "Escola não vinculada."
                _criando.value = false
                return@launch
            }

            val validFrom = isoFormatter.format(java.util.Date(validFromMs))
            val validUntil = isoFormatter.format(java.util.Date(validUntilMs))

            val request = CreatePickupAuthorizationRequest(
                schoolId = schoolId,
                studentId = studentId,
                authorizedBy = savedUser.id,
                authorizedPerson = CreateAuthorizedPerson(
                    name = nome.trim(),
                    document = documento.trim(),
                    relationship = relacao.trim()
                ),
                qrCode = UUID.randomUUID().toString(),
                validFrom = validFrom,
                validUntil = validUntil
            )

            try {
                val response = RetrofitClient.autorizacaoSaidaApi.criarAutorizacao("Bearer $token", request)
                if (response.error) {
                    _criarErro.value = response.getErrorMessage()
                } else {
                    _showNovaAutorizacaoSheet.value = false
                    loadAutorizacoes()
                }
            } catch (e: retrofit2.HttpException) {
                _criarErro.value = "Erro ao criar autorização (${e.code()})."
            } catch (e: java.net.UnknownHostException) {
                _criarErro.value = "Sem conexão com a internet."
            } catch (e: java.net.SocketTimeoutException) {
                _criarErro.value = "Tempo de conexão esgotado."
            } catch (e: Exception) {
                _criarErro.value = "Erro ao criar autorização."
            } finally {
                _criando.value = false
            }
        }
    }

    private fun AutorizacaoSaidaDoc.toUi(): AutorizacaoSaida {
        val status = when {
            !active  -> "Cancelado"
            used     -> "Utilizado"
            else     -> "Aguardando saída"
        }
        val firstName = student?.fullName?.trim()?.split(" ")?.firstOrNull() ?: "Aluno"
        val validAte = runCatching { isoFormatter.parse(validUntil)?.let { dateFormatter.format(it) } }
            .getOrNull() ?: validUntil
        val classId = student?.memberships?.firstOrNull { it.role == "student" }?.classId.orEmpty()

        return AutorizacaoSaida(
            id = id,
            schoolId = school?.id.orEmpty(),
            studentId = student?.id.orEmpty(),
            studentName = firstName,
            studentAvatarUrl = student?.avatarUrl?.takeIf { it.isNotBlank() },
            className = classNameById[classId].orEmpty(),
            status = status,
            autorizadoPor = authorizedPerson?.name.orEmpty(),
            autorizadoDocumento = authorizedPerson?.document.orEmpty(),
            relacao = authorizedPerson?.relationship.orEmpty(),
            validAte = validAte
        )
    }

    fun registrarSaida(autorizacao: AutorizacaoSaida) {
        viewModelScope.launch {
            _registrando.value = autorizacao.id
            val token = TokenManager.getAccessToken() ?: run {
                _registrando.value = null
                return@launch
            }
            val savedUser = TokenManager.getSavedUser() ?: run {
                _registrando.value = null
                return@launch
            }
            try {
                RetrofitClient.autorizacaoSaidaApi.criarPickupLog(
                    "Bearer $token",
                    CreatePickupLogRequest(
                        schoolId = autorizacao.schoolId,
                        studentId = autorizacao.studentId,
                        authorizationId = autorizacao.id,
                        method = "manual",
                        pickedUpBy = PickedUpBy(
                            name = autorizacao.autorizadoPor,
                            document = autorizacao.autorizadoDocumento,
                            relationship = autorizacao.relacao.takeIf { it.isNotBlank() }
                        ),
                        verifiedBy = savedUser.id,
                        departureTime = isoFormatter.format(java.util.Date())
                    )
                )
                loadAutorizacoes()
                loadPickupLogs()
            } catch (_: Exception) {
            } finally {
                _registrando.value = null
            }
        }
    }

    fun reverterSaida(logId: String, authorizationId: String) {
        viewModelScope.launch {
            _revertendo.value = logId
            val token = TokenManager.getAccessToken() ?: run {
                _revertendo.value = null
                return@launch
            }
            try {
                RetrofitClient.autorizacaoSaidaApi.deletarPickupLog("Bearer $token", logId)
                if (authorizationId.isNotBlank()) {
                    RetrofitClient.autorizacaoSaidaApi.patchAutorizacaoUsed(
                        "Bearer $token",
                        authorizationId,
                        PatchAutorizacaoUsedRequest(used = false)
                    )
                }
                loadPickupLogs()
            } catch (_: Exception) {
            } finally {
                _revertendo.value = null
            }
        }
    }

    private fun dev.fslab.comunicacao.escolar.model.PickupLogDoc.toPickupLogUi(): PickupLogUi {
        val hora = runCatching {
            val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val local = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
            iso.parse(departureTime)?.let { local.format(it) }
        }.getOrNull() ?: departureTime
        val classId = student?.memberships?.firstOrNull { it.role == "student" }?.classId.orEmpty()
        return PickupLogUi(
            id = id,
            authorizationId = authorization?.id.orEmpty(),
            studentName = student?.fullName?.trim()?.split(" ")?.firstOrNull() ?: "Aluno",
            className = classNameById[classId].orEmpty(),
            pickedUpByName = pickedUpBy?.name.orEmpty(),
            pickedUpByDocument = pickedUpBy?.document.orEmpty(),
            pickedUpByRelationship = pickedUpBy?.relationship.orEmpty(),
            departureTime = hora,
            verifiedByName = verifiedBy?.fullName.orEmpty()
        )
    }
}