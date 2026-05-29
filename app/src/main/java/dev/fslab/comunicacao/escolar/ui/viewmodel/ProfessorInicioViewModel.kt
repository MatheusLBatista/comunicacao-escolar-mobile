package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class AtividadeRecente(
    val actorName: String,
    val avatarUrl: String?,
    val studentName: String,
    val dateDisplay: String,
    val timeAgo: String
)

sealed class ProfessorInicioUiState {
    object Loading : ProfessorInicioUiState()
    object Empty : ProfessorInicioUiState()
    data class Error(val message: String) : ProfessorInicioUiState()
    data class Content(val atividades: List<AtividadeRecente>) : ProfessorInicioUiState()
}

class ProfessorInicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfessorInicioUiState>(ProfessorInicioUiState.Loading)
    val uiState: StateFlow<ProfessorInicioUiState> = _uiState.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR")).apply {
        timeZone = TimeZone.getDefault()
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfessorInicioUiState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _uiState.value = ProfessorInicioUiState.Error("Sessão expirada.")
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacoes(
                    token = "Bearer $token"
                )
                val docs = response.data?.docs.orEmpty().take(5)
                if (docs.isEmpty()) {
                    _uiState.value = ProfessorInicioUiState.Empty
                } else {
                    _uiState.value = ProfessorInicioUiState.Content(docs.map { it.toAtividade() })
                }
            } catch (_: java.net.UnknownHostException) {
                _uiState.value = ProfessorInicioUiState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _uiState.value = ProfessorInicioUiState.Error("Tempo de conexão esgotado.")
            } catch (_: Exception) {
                _uiState.value = ProfessorInicioUiState.Error("Erro ao carregar atividades recentes.")
            }
        }
    }

    private fun AutorizacaoSaidaDoc.toAtividade(): AtividadeRecente {
        val now = Date()
        val createdDate = runCatching { isoFormatter.parse(createdAt) }.getOrNull()

        val timeAgo = if (createdDate != null) {
            val diffMs = now.time - createdDate.time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)
            when {
                minutes < 1  -> "agora"
                hours < 1    -> "${minutes}m atrás"
                hours < 24   -> "${hours}h atrás"
                else         -> "${days}d atrás"
            }
        } else ""

        val dateDisplay = if (createdDate != null) {
            val todayStr = dateFormatter.format(now)
            val createdStr = dateFormatter.format(createdDate)
            if (createdStr == todayStr) "hoje às ${timeFormatter.format(createdDate)}" else createdStr
        } else ""

        return AtividadeRecente(
            actorName = authorizedBy?.fullName ?: "Responsável",
            avatarUrl = authorizedBy?.avatarUrl?.takeIf { it.isNotBlank() }?.fixLocalhostUrl(),
            studentName = student?.fullName ?: "Aluno",
            dateDisplay = dateDisplay,
            timeAgo = timeAgo
        )
    }

    // Em ambiente de desenvolvimento, a URL vinda do servidor usa "localhost" que não resolve no emulador Android — substitui pelo alias padrão do emulador.
    private fun String.fixLocalhostUrl(): String =
        replace("://localhost", "://10.0.2.2")
}