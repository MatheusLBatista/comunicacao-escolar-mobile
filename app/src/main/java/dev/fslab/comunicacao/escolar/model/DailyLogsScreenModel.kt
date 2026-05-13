package dev.fslab.comunicacao.escolar.model

data class DailyLog(
    val id: String,
    val childName: String,
    val time: String,
    val description: String,
    val date: String,
    val avatarRes: Int? = null,
    val timestamp: Long = 0L
)