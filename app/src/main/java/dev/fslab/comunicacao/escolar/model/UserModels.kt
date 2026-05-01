package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("full_name") val fullName: String? = null
)

data class UserResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: ApiUser? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data != null
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class ApiUser(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("auth_provider") val authProvider: String = "local",
    @SerializedName("memberships") val memberships: List<ApiMembership> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
) {
    fun toUser(): User {
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
            email = email ?: "",
            role = userRole,
            schoolId = activeMembership?.schoolId?.ifEmpty { null }
        )
    }
}
