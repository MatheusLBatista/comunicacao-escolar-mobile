package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaida
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    init {
        loadAutorizacoes()
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
                val list = response.data?.docs.orEmpty().map { it.toUi() }
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
                RetrofitClient.autorizacaoSaidaApi.cancelarAutorizacao("Bearer $token", id)
            } catch (_: Exception) {
            } finally {
                _cancelando.value = null
                loadAutorizacoes()
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
