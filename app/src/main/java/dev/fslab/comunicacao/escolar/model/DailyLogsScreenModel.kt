package dev.fslab.comunicacao.escolar.model

data class DailyLog(
    val id: String,
    val childName: String,
    val time: String,
    val description: String,
    val date: String,
    val avatarRes: Int? = null,
    val timestamp: Long = 0L,
    val teacherName: String = "",
    val teacherId: String = "",
    val observation: String = "",
    val isPresent: Boolean = true,
    val entries: List<DailyLogDetailEntry> = emptyList(),
    val conversationId: String = "",
    val studentAvatarUrl: String? = null,
    val teacherAvatarUrl: String? = null,
    val className: String = ""
)

data class DailyLogDetailEntry(
    val label: String,
    val value: String
)