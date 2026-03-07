package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.*
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Estados possíveis da autenticação
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * AuthViewModel - Gerencia o estado de autenticação da aplicação
 *
 * Responsável por:
 * - Login via API (POST /auth/login)
 * - Gerenciar tokens JWT e estado do usuário
 * - Logout e limpeza de sessão
 */
class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private val gson = Gson()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    init {
        TokenManager.onSessionExpired = {
            viewModelScope.launch {
                Log.w(TAG, "Sessão expirada — fazendo logout automático")
                _accessToken.value = null
                _currentUser.value = null
                _authState.value = AuthState.Error("Sessão expirada. Faça login novamente.")
            }
        }

        TokenManager.onTokensRefreshed = { newAccessToken ->
            viewModelScope.launch {
                Log.d(TAG, "Tokens renovados silenciosamente")
                _accessToken.value = newAccessToken
            }
        }
    }

    // ═══════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = LoginRequest(email = email, senha = password)
                val response = RetrofitClient.authApi.login(request)

                if (response.isSuccess()) {
                    val loginData = response.getLoginData()
                    if (loginData != null) {
                        TokenManager.saveTokens(loginData.token, loginData.refresh)
                        _accessToken.value = loginData.token

                        val usuario = loginData.usuario
                        if (usuario != null && usuario.papeis.isNotEmpty()) {
                            val user = usuario.toUser()
                            _currentUser.value = user
                            _authState.value = AuthState.Success(user)
                        } else {
                            val user = extractUserFromJwt(loginData.token, email)
                            if (user != null) {
                                _currentUser.value = user
                                _authState.value = AuthState.Success(user)
                            } else {
                                createBasicUser(email)
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error("Erro ao processar resposta do login")
                    }
                } else {
                    val errorMessage = response.getErrorMessage().ifEmpty { "Credenciais inválidas" }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Email ou senha incorretos"
                    403 -> "Usuário inativo ou bloqueado"
                    404 -> "Usuário não encontrado"
                    500 -> "Erro no servidor. Tente novamente mais tarde."
                    else -> "Erro de conexão: ${e.message()}"
                }
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: java.net.UnknownHostException) {
                _authState.value = AuthState.Error("Sem conexão com a internet")
            } catch (e: java.net.SocketTimeoutException) {
                _authState.value = AuthState.Error("Tempo de conexão esgotado")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Erro ao conectar: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

    // ═══════════════════════════════════════════
    // LOGOUT
    // ═══════════════════════════════════════════

    fun logout() {
        val currentToken = TokenManager.getAccessToken()

        TokenManager.clearTokens()
        _accessToken.value = null
        _currentUser.value = null
        _authState.value = AuthState.Idle

        viewModelScope.launch {
            try {
                currentToken?.let { token ->
                    RetrofitClient.authApi.logout("Bearer $token")
                }
            } catch (_: Exception) {
                // Ignora — sessão local já foi encerrada
            }
        }
    }

    // ═══════════════════════════════════════════
    // UTILITÁRIOS
    // ═══════════════════════════════════════════

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Extrai dados do usuário a partir do payload JWT
     */
    private fun extractUserFromJwt(token: String, email: String): User? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val decodedPayload = String(decodedBytes, Charsets.UTF_8)

            val json = JSONObject(decodedPayload)
            val id = json.optString("id", "")
            val papeisArray = json.optJSONArray("papeis")
            val papeis = mutableListOf<String>()
            if (papeisArray != null) {
                for (i in 0 until papeisArray.length()) {
                    papeis.add(papeisArray.getString(i))
                }
            }

            val userRole = when {
                papeis.any { it.uppercase().contains("ADMIN") } -> UserRole.ADMIN
                papeis.any { it.uppercase().contains("PROFESSOR") } -> UserRole.PROFESSOR
                else -> UserRole.RESPONSAVEL
            }

            User(
                id = id,
                nome = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                role = userRole
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao extrair user do JWT: ${e.message}")
            null
        }
    }

    private fun createBasicUser(email: String) {
        val user = User(
            id = "",
            nome = email.substringBefore("@"),
            email = email,
            role = UserRole.RESPONSAVEL
        )
        _currentUser.value = user
        _authState.value = AuthState.Success(user)
    }
}
