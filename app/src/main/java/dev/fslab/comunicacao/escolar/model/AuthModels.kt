package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════════════
// REQUEST models
// ══════════════════════════════════════════════

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String
)

data class RegisterRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String
)

data class RecoverPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordByCodeRequest(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("senha") val senha: String
)

data class RefreshRequest(
    @SerializedName("token") val token: String
)

// ══════════════════════════════════════════════
// RESPONSE models
// ══════════════════════════════════════════════

data class LoginResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: LoginData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data != null && data.token.isNotEmpty()
    fun getLoginData(): LoginData? = data
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class LoginData(
    @SerializedName("token") val token: String = "",
    @SerializedName("refresh") val refresh: String = "",
    @SerializedName("expiraEm") val expiraEm: String = "",
    @SerializedName("usuario") val usuario: LoginUsuario? = null
)

data class LoginUsuario(
    @SerializedName("id") val id: String = "",
    @SerializedName("_id") val mongoId: String = "",
    @SerializedName("nome") val nome: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("papeis") val papeis: List<String> = emptyList(),
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("fusoHorario") val fusoHorario: String = "America/Manaus"
) {
    fun obtemId(): String = if (id.isNotEmpty()) id else mongoId
}

/**
 * Converte LoginUsuario em User
 */
fun LoginUsuario.toUser(): User {
    val userRole = when {
        papeis.any { it.uppercase().contains("ADMIN") } -> UserRole.ADMIN
        papeis.any { it.uppercase().contains("PROFESSOR") } -> UserRole.PROFESSOR
        else -> UserRole.RESPONSAVEL
    }

    return User(
        id = obtemId(),
        nome = nome,
        email = email,
        role = userRole,
        avatar = avatar,
        fusoHorario = fusoHorario
    )
}

data class RegisterResponse(
    @SerializedName("token") val token: String = "",
    @SerializedName("refresh") val refresh: String = "",
    @SerializedName("expiraEm") val expiraEm: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = token.isNotEmpty()
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class RefreshResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: RefreshData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data != null && data.token.isNotEmpty()
}

data class RefreshData(
    @SerializedName("token") val token: String = "",
    @SerializedName("refresh") val refresh: String = "",
    @SerializedName("expiraEm") val expiraEm: String = ""
)

data class ApiErrorResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}
