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

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Registered(val message: String = "Conta criada! Faça login para continuar.") : AuthState()
    data class Error(val message: String) : AuthState()
}

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

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = LoginRequest(email = email, password = password)
                val response = RetrofitClient.authApi.login(request)

                if (response.isSuccess()) {
                    val apiUser = response.getLoginData()?.user
                    if (apiUser != null) {
                        TokenManager.saveTokens(apiUser.accessToken, apiUser.refreshToken)
                        _accessToken.value = apiUser.accessToken

                        val user = if (apiUser.memberships.isNotEmpty()) {
                            apiUser.toUser()
                        } else {
                            extractUserFromJwt(apiUser.accessToken, apiUser.email)
                                ?: User(
                                    id = apiUser.id,
                                    nome = apiUser.fullName.ifEmpty { email.substringBefore("@") },
                                    email = apiUser.email.ifEmpty { email },
                                    role = UserRole.RESPONSAVEL,
                                    schoolId = null
                                )
                        }
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Error("Erro ao processar resposta do login")
                    }
                } else {
                    val errorMessage = response.getErrorMessage().ifEmpty { "Credenciais inválidas" }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "E-mail ou senha inválidos"
                    401 -> "E-mail ou senha incorretos"
                    403 -> "Usuário inativo ou sem acesso ao aplicativo"
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

    fun registerUser(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = RegisterRequest(fullName = fullName, email = email, password = password)
                val response = RetrofitClient.authApi.register(request)

                if (response.isSuccess()) {
                    _authState.value = AuthState.Registered()
                } else {
                    val errorMessage = response.getErrorMessage().ifEmpty { "Erro ao criar conta" }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    409 -> "Este e-mail já está cadastrado"
                    400 -> "Dados inválidos. Verifique os campos."
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
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun updateCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = GoogleLoginRequest(idToken = idToken)
                val response = RetrofitClient.authApi.loginWithGoogle(request)

                if (response.isSuccess()) {
                    val apiUser = response.getLoginData()?.user
                    if (apiUser != null) {
                        TokenManager.saveTokens(apiUser.accessToken, apiUser.refreshToken)
                        _accessToken.value = apiUser.accessToken

                        val user = if (apiUser.memberships.isNotEmpty()) {
                            apiUser.toUser()
                        } else {
                            extractUserFromJwt(apiUser.accessToken, apiUser.email)
                                ?: User(
                                    id = apiUser.id,
                                    nome = apiUser.fullName.ifEmpty { apiUser.email.substringBefore("@") },
                                    email = apiUser.email,
                                    role = UserRole.RESPONSAVEL,
                                    schoolId = null
                                )
                        }
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Error("Erro ao processar login com Google.")
                    }
                } else {
                    _authState.value = AuthState.Error("Falha no login com Google.")
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Token do Google inválido ou expirado. Tente novamente."
                    500 -> "Google Sign-In não configurado no servidor."
                    else -> "Erro ao fazer login com Google (${e.code()})."
                }
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: java.net.UnknownHostException) {
                _authState.value = AuthState.Error("Sem conexão com a internet")
            } catch (e: java.net.SocketTimeoutException) {
                _authState.value = AuthState.Error("Tempo de conexão esgotado")
            } catch (e: Exception) {
                Log.e(TAG, "Erro no Google login", e)
                _authState.value = AuthState.Error("Erro ao conectar: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

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
