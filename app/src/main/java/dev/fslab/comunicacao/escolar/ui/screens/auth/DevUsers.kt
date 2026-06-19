package dev.fslab.comunicacao.escolar.ui.screens.auth

data class DevUser(val label: String, val email: String, val password: String)

val devUsers = listOf(
    DevUser("Admin", "admin@admin.com", "Senha@123"),
    DevUser("Professor", "teacher@teacher.com", "Senha@123"),
    DevUser("Responsável", "parent@parent.com", "Senha@123"),
)
