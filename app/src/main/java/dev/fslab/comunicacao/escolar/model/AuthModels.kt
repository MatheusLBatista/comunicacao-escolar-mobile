package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
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

data class GoogleLoginRequest(
    @SerializedName("id_token") val idToken: String
)

data class LoginResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: LoginData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data?.user?.accessToken?.isNotEmpty() == true
    fun getLoginData(): LoginData? = data
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class LoginData(
    @SerializedName("user") val user: ApiLoginUser? = null
)

data class ApiLoginUser(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("refresh_token") val refreshToken: String = "",
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("memberships") val memberships: List<ApiMembership> = emptyList()
)

data class ApiMembership(
    @SerializedName("school_id") val schoolId: String = "",
    @SerializedName("role") val role: String = ""
)

fun ApiLoginUser.toUser(): User {
    val activeMembership = memberships.firstOrNull()
    val userRole = when (activeMembership?.role) {
        "admin"   -> UserRole.ADMIN
        "teacher" -> UserRole.PROFESSOR
        "parent"  -> UserRole.RESPONSAVEL
        else      -> UserRole.RESPONSAVEL
    }
    return User(
        id = id,
        nome = fullName,
        email = email,
        role = userRole,
        schoolId = activeMembership?.schoolId?.ifEmpty { null }
    )
}

data class RegisterResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: RegisteredUser? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data != null
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class RegisteredUser(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email") val email: String = ""
)

data class RefreshResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: RefreshData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data?.accessToken?.isNotEmpty() == true
}

data class RefreshData(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("refresh_token") val refreshToken: String = ""
)

data class ApiErrorResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}
