package dev.fslab.comunicacao.escolar.model

data class DailyLog(
    val id: Int,
    val childName: String,
    val time: String,
    val description: String,
    val date: String,
    val avatarRes: Int? = null  // ID do drawable, ex: R.drawable.avatar_leo
)