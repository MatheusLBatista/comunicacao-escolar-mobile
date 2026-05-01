package dev.fslab.comunicacao.escolar.model

enum class UserRole {
    ADMIN,
    PROFESSOR,
    RESPONSAVEL
}

data class User(
    val id: String,
    val nome: String,
    val email: String,
    val role: UserRole,
    val avatar: String? = null,
    val fusoHorario: String = "America/Manaus",
    val schoolId: String? = null
)
