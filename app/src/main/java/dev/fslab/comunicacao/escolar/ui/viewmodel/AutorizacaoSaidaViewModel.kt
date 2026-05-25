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
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoRequest
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

class AutorizacaoSaidaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AutorizacaoSaidaUiState>(AutorizacaoSaidaUiState.Loading)
    val uiState: StateFlow<AutorizacaoSaidaUiState> = _uiState.asStateFlow()

    private val _cancelando = MutableStateFlow<String?>(null)
    val cancelando: StateFlow<String?> = _cancelando.asStateFlow()

    private val _showNovaAutorizacaoSheet = MutableStateFlow(false)
    val showNovaAutorizacaoSheet: StateFlow<Boolean> = _showNovaAutorizacaoSheet.asStateFlow()

    private val _criando = MutableStateFlow(false)
    val criando: StateFlow<Boolean> = _criando.asStateFlow()

    private val _criarErro = MutableStateFlow<String?>(null)
    val criarErro: StateFlow<String?> = _criarErro.asStateFlow()

    private val _qrCodeId = MutableStateFlow<String?>(null)
    val qrCodeId: StateFlow<String?> = _qrCodeId.asStateFlow()

    private val _rawDocs = MutableStateFlow<List<AutorizacaoSaidaDoc>>(emptyList())

    private val _alunos = MutableStateFlow<List<ApiAssociatedStudent>>(emptyList())
    val alunos: StateFlow<List<ApiAssociatedStudent>> = _alunos.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    init {
        loadAutorizacoes()
        loadAlunos()
    }

    private fun loadAlunos() {
        val json = TokenManager.getStudentsJson() ?: return
        try {
            val type = object : TypeToken<List<ApiAssociatedStudent>>() {}.type
            _alunos.value = Gson().fromJson(json, type)
        } catch (_: Exception) {}
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
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacoes("Bearer $token")
                if (response.error) {
                    _uiState.value = AutorizacaoSaidaUiState.Error(response.getErrorMessage())
                    return@launch
                }
                val docs = response.data?.docs.orEmpty()
                _rawDocs.value = docs
                val list = docs.filter { it.active }.map { it.toUi() }
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

    fun mostrarQrCode(id: String) {
        _qrCodeId.value = id
    }

    fun dispensarQrCode() {
        _qrCodeId.value = null
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
                    _qrCodeId.value = response.data?.id
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

        return AutorizacaoSaida(
            id = id,
            studentName = firstName,
            studentAvatarUrl = student?.avatarUrl?.takeIf { it.isNotBlank() },
            status = status,
            autorizadoPor = authorizedPerson?.name.orEmpty(),
            relacao = authorizedPerson?.relationship.orEmpty(),
            validAte = validAte
        )
    }
}