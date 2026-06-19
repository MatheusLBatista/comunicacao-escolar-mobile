package dev.fslab.comunicacao.escolar.util

import dev.fslab.comunicacao.escolar.network.TokenManager
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {

    private fun userZone(): ZoneId = try {
        ZoneId.of(TokenManager.getUserTimezone())
    } catch (_: Exception) {
        ZoneId.systemDefault()
    }

    fun getTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val past = Instant.parse(isoString)
            val now = Instant.now()
            val duration = Duration.between(past, now)

            when {
                duration.toMinutes() < 1 -> "Agora"
                duration.toMinutes() < 60 -> "${duration.toMinutes()}m atrás"
                duration.toHours() < 24 -> "${duration.toHours()}h atrás"
                duration.toDays() < 7 -> "${duration.toDays()}d atrás"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(userZone())
                    formatter.format(past)
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getAuditFormat(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val zone = userZone()
            val instant = Instant.parse(isoString)
            val zoned = instant.atZone(zone)
            val today = LocalDate.now(zone)
            val timeStr = zoned.format(DateTimeFormatter.ofPattern("HH:mm"))
            when (zoned.toLocalDate()) {
                today -> "Hoje, $timeStr"
                today.minusDays(1) -> "Ontem, $timeStr"
                else -> zoned.format(DateTimeFormatter.ofPattern("dd/MM, HH:mm"))
            }
        } catch (e: Exception) {
            isoString
        }
    }
}
