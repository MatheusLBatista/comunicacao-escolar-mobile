package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dev.fslab.comunicacao.escolar.model.*
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.SocketManager
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Registered(val message: String = "Conta criada! Faça login para continuar.") : AuthState()
    data class Error(val message: String) : AuthState()
    data class RecoverEmailSent(val message: String = "Código enviado com sucesso. Verifique seu e-mail.") : AuthState()
    data class PasswordResetSuccess(val message: String = "Senha atualizada com sucesso! Faça login.") : AuthState()
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

        val savedToken = TokenManager.getAccessToken()
        val savedUser = TokenManager.getSavedUser()
        if (!savedToken.isNullOrEmpty() && savedUser != null) {
            val role = when (savedUser.role) {
                "admin"   -> UserRole.ADMIN
                "teacher" -> UserRole.PROFESSOR
                "parent"  -> UserRole.RESPONSAVEL
                else      -> UserRole.RESPONSAVEL
            }
            val user = User(
                id = savedUser.id,
                nome = savedUser.name,
                email = savedUser.email,
                role = role,
                schoolId = savedUser.schoolId
            )
            _accessToken.value = savedToken
            _currentUser.value = user
            _authState.value = AuthState.Success(user)
            Log.d(TAG, "Sessão restaurada para ${user.email} (${savedUser.role})")
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
                        TokenManager.saveTokens(
                            apiUser.accessToken, apiUser.refreshToken,
                            TokenManager.UserInfo(user.id, user.nome, user.email, roleToString(user.role), user.schoolId)
                        )
                        val students = apiUser.memberships.flatMap { it.associatedStudents }.distinctBy { it.id }
                        TokenManager.saveStudentsJson(gson.toJson(students))
                        _accessToken.value = apiUser.accessToken
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                        syncFcmToken()
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
                    403 -> "Conta não vinculada a nenhuma escola. Entre em contato com o administrador."
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
                    403 -> "E-mail não cadastrado no sistema. Entre em contato com o administrador da escola."
                    409 -> "Esta conta já está ativa. Faça login."
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

        SocketManager.disconnect()
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
                        TokenManager.saveTokens(
                            apiUser.accessToken, apiUser.refreshToken,
                            TokenManager.UserInfo(user.id, user.nome, user.email, roleToString(user.role), user.schoolId)
                        )
                        val students = apiUser.memberships.flatMap { it.associatedStudents }.distinctBy { it.id }
                        TokenManager.saveStudentsJson(gson.toJson(students))
                        _accessToken.value = apiUser.accessToken
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                        syncFcmToken()
                    } else {
                        _authState.value = AuthState.Error("Erro ao processar login com Google.")
                    }
                } else {
                    _authState.value = AuthState.Error("Falha no login com Google.")
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Token do Google inválido ou expirado. Tente novamente."
                    403 -> "E-mail não cadastrado no sistema. Entre em contato com o administrador da escola."
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

    fun recoverPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = RecoverPasswordRequest(email)
                RetrofitClient.authApi.recoverPassword(request)
                _authState.value = AuthState.RecoverEmailSent()
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    404 -> "E-mail não encontrado no sistema."
                    else -> "Erro ao solicitar recuperação (${e.code()})."
                }
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Erro ao conectar: ${e.localizedMessage}")
            }
        }
    }

    fun resetPasswordByCode(codigo: String, novaSenha: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = ResetPasswordByCodeRequest(codigo, novaSenha)
                RetrofitClient.authApi.resetPasswordByCode(request)
                _authState.value = AuthState.PasswordResetSuccess()
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Senha muito fraca. Deve ter no mínimo 8 caracteres com 1 letra, 1 número e 1 caractere especial."
                    401 -> "Código inválido ou expirado."
                    404 -> "Código não encontrado."
                    else -> "Erro ao redefinir senha (${e.code()})."
                }
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Erro ao conectar: ${e.localizedMessage}")
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
            val jwtEmail = json.optString("email", "").ifEmpty { json.optString("sub", "") }
            val resolvedEmail = jwtEmail.ifEmpty { email }
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
                nome = resolvedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = resolvedEmail,
                role = userRole
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao extrair user do JWT: ${e.message}")
            null
        }
    }

    private fun roleToString(role: UserRole): String = when (role) {
        UserRole.ADMIN       -> "admin"
        UserRole.PROFESSOR   -> "teacher"
        UserRole.RESPONSAVEL -> "parent"
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            try {
                // Tenta obter o token atual do Firebase
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM Token obtido para sincronização: $token")

                // Salva localmente de forma segura
                TokenManager.saveFcmToken(token)

                // Envia para a API via novo endpoint
                val request = FcmTokenRequest(fcmToken = token)
                RetrofitClient.userApi.updateFcmToken(request)
                Log.d(TAG, "FCM Token sincronizado com o servidor com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao sincronizar FCM Token: ${e.message}")
            }
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
