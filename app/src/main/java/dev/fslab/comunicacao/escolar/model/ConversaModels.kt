package dev.fslab.comunicacao.escolar.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import dev.fslab.comunicacao.escolar.network.TokenManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ApiConversation(
    @SerializedName("_id") val id: String = "",
    @SerializedName("school_id") val schoolId: String = "",
    @SerializedName("participants") val participants: List<JsonElement> = emptyList(),
    @SerializedName("type") val type: String = "private",
    @SerializedName("last_message_at") val lastMessageAt: String? = null,
    @SerializedName("last_message_text") val lastMessageText: String? = null,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("created_at") val createdAt: String = ""
)

data class ApiMessage(
    @SerializedName("_id") val id: String = "",
    @SerializedName("conversation_id") val conversationId: String = "",
    // sender_id is String (after POST /messages) OR {_id, full_name, email} (after GET /messages)
    @SerializedName("sender_id") val senderIdRaw: JsonElement? = null,
    @SerializedName("text") val text: String = "",
    @SerializedName("read_by") val readBy: List<ApiReadEntry> = emptyList(),
    @SerializedName("sent_at") val sentAt: String = "",
    @SerializedName("active") val active: Boolean = true
)

data class ApiReadEntry(
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("at") val at: String = ""
)

data class CreateConversationRequest(
    @SerializedName("participant_id") val participantId: String,
    @SerializedName("type") val type: String = "private"
)

data class SendMessageRequest(
    @SerializedName("text") val text: String
)

data class MarkReadResponse(
    @SerializedName("marked") val marked: Int = 0
)

data class ReadReceiptEvent(
    @SerializedName("conversation_id") val conversationId: String = "",
    @SerializedName("user_id") val userId: String = ""
)

data class Participant(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String? = null
)

data class Conversation(
    val id: String,
    val schoolId: String,
    val otherParticipant: Participant,
    val lastMessageAt: String?,
    val lastMessageAtFormatted: String,
    val type: String,
    val avatarUrl: String? = null
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val sentAt: String,
    val sentAtFormatted: String,
    val readBy: List<String>,
    val isFromMe: Boolean
)

private fun JsonElement.extractParticipantId(): String = when {
    isJsonObject -> asJsonObject.get("_id")?.asString ?: ""
    isJsonPrimitive -> asString
    else -> ""
}

private fun JsonElement.extractParticipantName(): String = when {
    isJsonObject -> asJsonObject.get("full_name")?.asString ?: ""
    else -> ""
}

private fun JsonElement.extractParticipantEmail(): String = when {
    isJsonObject -> asJsonObject.get("email")?.asString ?: ""
    else -> ""
}

private fun JsonElement.extractParticipantAvatarUrl(): String? = when {
    isJsonObject -> asJsonObject.get("avatar_url")?.asString
    else -> null
}

private fun JsonElement.toParticipant(): Participant = Participant(
    id = extractParticipantId(),
    fullName = extractParticipantName(),
    email = extractParticipantEmail(),
    avatarUrl = extractParticipantAvatarUrl()
)

private fun formatMessageTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(isoString)
        val zone = try { ZoneId.of(TokenManager.getUserTimezone()) } catch (_: Exception) { ZoneId.systemDefault() }
        val zoned = instant.atZone(zone)
        val today = LocalDate.now(zone)
        when (zoned.toLocalDate()) {
            today -> zoned.format(DateTimeFormatter.ofPattern("HH:mm"))
            today.minusDays(1) -> "ontem"
            else -> zoned.format(DateTimeFormatter.ofPattern("dd/MM"))
        }
    } catch (_: Exception) {
        isoString
    }
}

fun formatMessageDateSeparator(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(isoString)
        val zone = try { ZoneId.of(TokenManager.getUserTimezone()) } catch (_: Exception) { ZoneId.systemDefault() }
        val zoned = instant.atZone(zone)
        val today = LocalDate.now(zone)
        when (zoned.toLocalDate()) {
            today -> "Hoje"
            today.minusDays(1) -> "Ontem"
            else -> zoned.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    } catch (_: Exception) {
        ""
    }
}

fun ApiConversation.toConversation(currentUserId: String): Conversation {
    val otherParticipant = participants
        .map { it.toParticipant() }
        .firstOrNull { it.id != currentUserId }
        ?: participants.firstOrNull()?.toParticipant()
        ?: Participant("", "Desconhecido", "")
    return Conversation(
        id = id,
        schoolId = schoolId,
        otherParticipant = otherParticipant,
        lastMessageAt = lastMessageAt,
        lastMessageAtFormatted = formatMessageTime(lastMessageAt),
        type = type,
        avatarUrl = otherParticipant.avatarUrl
    )
}

fun ApiMessage.toMessage(currentUserId: String): Message {
    val senderId = senderIdRaw?.extractParticipantId() ?: ""
    val senderName = senderIdRaw?.extractParticipantName() ?: ""
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        sentAt = sentAt,
        sentAtFormatted = formatMessageTime(sentAt),
        readBy = readBy.map { it.userId },
        isFromMe = senderId == currentUserId
    )
}
