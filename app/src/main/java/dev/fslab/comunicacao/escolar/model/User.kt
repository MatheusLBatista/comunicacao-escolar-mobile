package dev.fslab.comunicacao.escolar.model

/**
 * UserRole - Tipos de usuários do sistema Comunicação Escolar
 */
enum class UserRole {
    ADMIN,
    PROFESSOR,
    RESPONSAVEL
}

/**
 * User - Representa um usuário autenticado
 */
data class User(
    val id: String,
    val nome: String,
    val email: String,
    val role: UserRole,
    val avatar: String? = null,
    val fusoHorario: String = "America/Manaus"
)
