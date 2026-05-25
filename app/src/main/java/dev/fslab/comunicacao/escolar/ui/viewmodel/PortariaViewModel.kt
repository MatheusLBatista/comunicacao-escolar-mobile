package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.model.CreatePickupLogRequest
import dev.fslab.comunicacao.escolar.model.PickedUpBy
import dev.fslab.comunicacao.escolar.model.User
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
import java.util.concurrent.atomic.AtomicBoolean

sealed class PortariaState {
    object Scanning : PortariaState()
    object Loading : PortariaState()
    data class Detail(
        val doc: AutorizacaoSaidaDoc,
        val isValid: Boolean,
        val invalidReason: String? = null
    ) : PortariaState()
    data class Error(val message: String) : PortariaState()
    object Confirming : PortariaState()
    data class Success(val studentName: String) : PortariaState()
}

class PortariaViewModel : ViewModel() {

    private val _state = MutableStateFlow<PortariaState>(PortariaState.Scanning)
    val state: StateFlow<PortariaState> = _state.asStateFlow()

    private val isProcessing = AtomicBoolean(false)

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun onQrDetected(rawValue: String) {
        if (!isProcessing.compareAndSet(false, true)) return
        fetchAutorizacao(rawValue.trim())
    }

    private fun fetchAutorizacao(id: String) {
        viewModelScope.launch {
            _state.value = PortariaState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _state.value = PortariaState.Error("Sessão expirada. Faça login novamente.")
                isProcessing.set(false)
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacaoById("Bearer $token", id)
                val doc = response.data
                if (response.error || doc == null) {
                    _state.value = PortariaState.Error(
                        response.getErrorMessage().ifBlank { "Autorização não encontrada." }
                    )
                    return@launch
                }
                val v = validate(doc)
                _state.value = PortariaState.Detail(doc, v.first, v.second)
            } catch (e: retrofit2.HttpException) {
                _state.value = PortariaState.Error(
                    if (e.code() == 404) "Autorização não encontrada."
                    else "Erro ao buscar autorização (${e.code()})."
                )
            } catch (_: java.net.UnknownHostException) {
                _state.value = PortariaState.Error("Sem conexão com a internet.")
            } catch (_: Exception) {
                _state.value = PortariaState.Error("Erro ao buscar autorização.")
            }
        }
    }

    private fun validate(doc: AutorizacaoSaidaDoc): Pair<Boolean, String?> {
        val now = Date()
        val validFrom = runCatching { isoFormatter.parse(doc.validFrom) }.getOrNull()
        val validUntil = runCatching { isoFormatter.parse(doc.validUntil) }.getOrNull()
        return when {
            !doc.active -> false to "Autorização cancelada"
            doc.used    -> false to "Autorização já utilizada"
            validFrom != null && now.before(validFrom) -> false to "Autorização ainda não é válida"
            validUntil != null && now.after(validUntil) -> false to "Autorização expirada"
            else -> true to null
        }
    }

    fun confirmarSaida(doc: AutorizacaoSaidaDoc, user: User) {
        viewModelScope.launch {
            _state.value = PortariaState.Confirming
            val token = TokenManager.getAccessToken() ?: run {
                _state.value = PortariaState.Error("Sessão expirada.")
                return@launch
            }
            val schoolId = user.schoolId ?: run {
                _state.value = PortariaState.Error("Escola não vinculada à sua conta.")
                return@launch
            }
            try {
                val request = CreatePickupLogRequest(
                    schoolId = schoolId,
                    studentId = doc.student?.id ?: "",
                    authorizationId = doc.id,
                    method = "qr_code",
                    pickedUpBy = PickedUpBy(
                        name = doc.authorizedPerson?.name ?: "",
                        document = doc.authorizedPerson?.document ?: ""
                    ),
                    verifiedBy = user.id,
                    departureTime = isoFormatter.format(Date())
                )
                val response = RetrofitClient.autorizacaoSaidaApi.criarPickupLog("Bearer $token", request)
                if (response.error) {
                    _state.value = PortariaState.Error(response.getErrorMessage())
                } else {
                    val studentFirst = doc.student?.fullName?.trim()?.split(" ")?.firstOrNull() ?: "Aluno"
                    _state.value = PortariaState.Success(studentFirst)
                }
            } catch (e: retrofit2.HttpException) {
                _state.value = PortariaState.Error("Erro ao registrar saída (${e.code()}).")
            } catch (_: java.net.UnknownHostException) {
                _state.value = PortariaState.Error("Sem conexão com a internet.")
            } catch (_: Exception) {
                _state.value = PortariaState.Error("Erro ao registrar saída.")
            }
        }
    }

    fun resetToScanning() {
        isProcessing.set(false)
        _state.value = PortariaState.Scanning
    }
}
