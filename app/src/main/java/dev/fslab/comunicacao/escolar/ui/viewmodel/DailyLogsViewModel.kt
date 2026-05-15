package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.DailyLog
import dev.fslab.comunicacao.escolar.model.DailyLogDoc
import dev.fslab.comunicacao.escolar.model.DailyLogDetailEntry
import dev.fslab.comunicacao.escolar.model.DailyLogEntry
import dev.fslab.comunicacao.escolar.model.DailyLogTemplateField
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

sealed class DailyLogsUiState {
    object Loading : DailyLogsUiState()
    data class Error(val message: String) : DailyLogsUiState()
    object Empty : DailyLogsUiState()
    data class Content(val groups: List<DailyLogsGroup>) : DailyLogsUiState()
}

data class DailyLogsGroup(
    val dateLabel: String,
    val logs: List<DailyLog>
)

class DailyLogsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DailyLogsUiState>(DailyLogsUiState.Loading)
    val uiState: StateFlow<DailyLogsUiState> = _uiState.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR"))

    init {
        loadDailyLogs()
    }

    fun loadDailyLogs() {
        viewModelScope.launch {
            _uiState.value = DailyLogsUiState.Loading

            val accessToken = dev.fslab.comunicacao.escolar.network.TokenManager.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                _uiState.value = DailyLogsUiState.Error("Sessão expirada. Faça login novamente.")
                return@launch
            }

            try {
                val response = RetrofitClient.dailyLogsApi.getDailyLogs("Bearer $accessToken")

                if (response.error) {
                    _uiState.value = DailyLogsUiState.Error(response.getErrorMessage())
                    return@launch
                }

                val docs = response.data?.docs.orEmpty()
                if (docs.isEmpty()) {
                    _uiState.value = DailyLogsUiState.Empty
                    return@launch
                }

                val groups = buildGroups(docs)
                _uiState.value = DailyLogsUiState.Content(groups)
            } catch (e: retrofit2.HttpException) {
                val message = if (e.code() == 498) {
                    "Sessão expirada. Faça login novamente."
                } else {
                    "Erro ao carregar atividades (${e.code()})."
                }
                _uiState.value = DailyLogsUiState.Error(message)
            } catch (e: java.net.UnknownHostException) {
                _uiState.value = DailyLogsUiState.Error("Sem conexão com a internet.")
            } catch (e: java.net.SocketTimeoutException) {
                _uiState.value = DailyLogsUiState.Error("Tempo de conexão esgotado.")
            } catch (e: Exception) {
                _uiState.value = DailyLogsUiState.Error("Erro ao carregar atividades.")
            }
        }
    }

    private fun buildGroups(docs: List<DailyLogDoc>): List<DailyLogsGroup> {
        val logs = docs.map { it.toUi() }
        val grouped = logs.groupBy { it.date }
        val sortedDates = grouped.keys.sortedByDescending { dateLabel ->
            parseDate(dateLabel)?.time ?: 0L
        }
        return sortedDates.map { dateLabel ->
            val logsForDate = grouped[dateLabel].orEmpty().sortedByDescending { it.timestamp }
            DailyLogsGroup(dateLabel = dateLabel, logs = logsForDate)
        }
    }

    private fun DailyLogDoc.toUi(): DailyLog {
        val parsedDate = parseIsoDate(date)
        val dateLabel = parsedDate?.let { dateFormatter.format(it) } ?: date
        val timeLabel = parsedDate?.let { timeFormatter.format(it) } ?: ""
        val description = buildDescription(observation, entries, isPresent)
        val studentName = buildStudentName(student?.fullName.orEmpty())
        val teacherName = buildTeacherName(teacher?.fullName.orEmpty())
        val detailEntries = buildDetailEntries(entries, isPresent, dailyLogTemplate?.fields.orEmpty())

        return DailyLog(
            id = id,
            childName = studentName,
            time = timeLabel,
            description = description,
            date = dateLabel,
            avatarRes = null,
            timestamp = parsedDate?.time ?: 0L,
            teacherName = teacherName,
            observation = observation,
            isPresent = isPresent,
            entries = detailEntries,
            conversationId = conversationId
        )
    }

    private fun buildDetailEntries(
        entries: List<DailyLogEntry>,
        isPresent: Boolean,
        templateFields: List<DailyLogTemplateField>
    ): List<DailyLogDetailEntry> {
        val labelByKey = templateFields
            .filter { it.key.isNotBlank() }
            .associate { it.key to it.label.trim() }

        val mappedEntries = entries.mapNotNull { entry ->
            val label = labelByKey[entry.fieldKey]?.ifBlank { null } ?: formatFallbackLabel(entry.fieldKey)
            val value = entry.value.trim()
            if (label.isBlank() || value.isBlank()) {
                null
            } else {
                DailyLogDetailEntry(label = label, value = value)
            }
        }

        val presenceValue = if (isPresent) "Sim" else "Não"
        return buildList {
            add(DailyLogDetailEntry(label = "Presença", value = presenceValue))
            addAll(mappedEntries)
        }
    }

    private fun formatFallbackLabel(fieldKey: String): String {
        return fieldKey
            .trim()
            .replace("_", " ")
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
    }

    private fun buildTeacherName(fullName: String): String {
        val trimmed = fullName.trim()
        if (trimmed.isBlank()) {
            return ""
        }
        return trimmed.split(" ").firstOrNull { it.isNotBlank() } ?: trimmed
    }

    private fun buildDescription(
        observation: String,
        entries: List<DailyLogEntry>,
        isPresent: Boolean
    ): String {
        if (observation.isNotBlank()) {
            return observation
        }

        if (!isPresent) {
            val absenceReason = entries.firstOrNull { it.fieldKey == "absence_reason" }?.value
            return absenceReason ?: "Aluno ausente."
        }

        val mood = entries.firstOrNull { it.fieldKey == "mood_status" }?.value
        val participation = entries.firstOrNull { it.fieldKey == "participation" }?.value
        val food = entries.firstOrNull { it.fieldKey == "food_intake" }?.value

        val parts = listOfNotNull(
            mood?.let { "Humor: $it" },
            participation?.let { "Participação: $it" },
            food?.let { "Alimentação: $it" }
        )

        return parts.joinToString(" • ").ifBlank { "Sem observações." }
    }

    private fun buildStudentName(fullName: String): String {
        val trimmed = fullName.trim()
        if (trimmed.isBlank()) {
            return "Aluno"
        }
        return trimmed.split(" ").firstOrNull { it.isNotBlank() } ?: trimmed
    }

    private fun parseIsoDate(value: String): java.util.Date? {
        return runCatching { isoFormatter.parse(value) }.getOrNull()
    }

    private fun parseDate(value: String): java.util.Date? {
        return runCatching { dateFormatter.parse(value) }.getOrNull()
    }
}
