package dev.fslab.comunicacao.escolar.model

data class DailyLog(
    val id: Int,
    val childName: String,
    val time: String,
    val description: String,
    val date: String
)