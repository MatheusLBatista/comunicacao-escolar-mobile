package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.DailyLogDoc
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class ClassDiarioStatus(
    val classId: String,
    val className: String,
    val studentCount: Int,
    val logCount: Int
) {
    val isDone: Boolean get() = studentCount > 0 && logCount >= studentCount
}

data class DiarioDateGroup(
    val dateLabel: String,
    val dateMs: Long,
    val turmas: List<ClassDiarioStatus>
)

sealed class DiarioDeBordoListUiState {
    object Loading : DiarioDeBordoListUiState()
    data class Error(val message: String) : DiarioDeBordoListUiState()
    data class Content(
        val pendentes: List<ClassDiarioStatus>,
        val concluidasPorData: List<DiarioDateGroup>
    ) : DiarioDeBordoListUiState()
}

class DiarioDeBordoListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DiarioDeBordoListUiState>(DiarioDeBordoListUiState.Loading)
    val uiState: StateFlow<DiarioDeBordoListUiState> = _uiState.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        load()
    }

    fun load() {
        val user = TokenManager.getSavedUser()
        val teacherId = user?.id ?: ""
        val schoolId = user?.schoolId ?: ""

        viewModelScope.launch {
            _uiState.value = DiarioDeBordoListUiState.Loading
            if (teacherId.isBlank() || schoolId.isBlank()) {
                _uiState.value = DiarioDeBordoListUiState.Error("Dados do usuário não encontrados.")
                return@launch
            }
            try {
                val (todayFrom, todayTo) = todayRange()
                val histFrom = historyStart(30)

                lateinit var classesList: List<dev.fslab.comunicacao.escolar.model.ApiClass>
                lateinit var studentsList: List<dev.fslab.comunicacao.escolar.model.ApiSchoolUser>
                lateinit var todayLogsList: List<DailyLogDoc>
                lateinit var histLogsList: List<DailyLogDoc>

                coroutineScope {
                    val classesDeferred = async {
                        RetrofitClient.adminApi.listClasses(
                            schoolId,
                            mapOf("teacher_id" to teacherId, "limit" to "100")
                        )
                    }
                    val studentsDeferred = async {
                        RetrofitClient.adminApi.listUsers(
                            schoolId,
                            mapOf("role" to "student", "limit" to "100")
                        )
                    }
                    val todayLogsDeferred = async {
                        RetrofitClient.dailyLogsApi.getDailyLogsFiltered(
                            mapOf(
                                "school_id" to schoolId,
                                "teacher_id" to teacherId,
                                "date_from" to todayFrom,
                                "date_to" to todayTo
                            )
                        )
                    }
                    val histLogsDeferred = async {
                        RetrofitClient.dailyLogsApi.getDailyLogsFiltered(
                            mapOf(
                                "school_id" to schoolId,
                                "teacher_id" to teacherId,
                                "date_from" to histFrom,
                                "date_to" to todayTo,
                                "limit" to "100"
                            )
                        )
                    }
                    classesList = classesDeferred.await().data?.docs.orEmpty()
                    studentsList = studentsDeferred.await().data?.docs.orEmpty()
                    todayLogsList = todayLogsDeferred.await().data?.docs.orEmpty()
                    histLogsList = histLogsDeferred.await().data?.docs.orEmpty()
                }

                val studentsByClass: Map<String, Set<String>> = classesList.associate { cls ->
                    cls.id to studentsList
                        .filter { u -> u.memberships.any { m -> m.role == "student" && m.classId == cls.id } }
                        .map { it.id }
                        .toSet()
                }

                val todayLoggedIds = todayLogsList.mapNotNull { it.student?.id }.toSet()
                val pendentes = classesList.map { cls ->
                    val classStudentIds = studentsByClass[cls.id] ?: emptySet()
                    ClassDiarioStatus(
                        classId = cls.id,
                        className = cls.name,
                        studentCount = classStudentIds.size,
                        logCount = classStudentIds.intersect(todayLoggedIds).size
                    )
                }.filter { !it.isDone }

                val concluidasPorData = buildConcluidasPorData(histLogsList, classesList, studentsByClass)

                _uiState.value = DiarioDeBordoListUiState.Content(
                    pendentes = pendentes,
                    concluidasPorData = concluidasPorData
                )
            } catch (_: java.net.UnknownHostException) {
                _uiState.value = DiarioDeBordoListUiState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _uiState.value = DiarioDeBordoListUiState.Error("Tempo de conexão esgotado.")
            } catch (_: Exception) {
                _uiState.value = DiarioDeBordoListUiState.Error("Erro ao carregar turmas.")
            }
        }
    }

    private fun buildConcluidasPorData(
        logs: List<DailyLogDoc>,
        classes: List<dev.fslab.comunicacao.escolar.model.ApiClass>,
        studentsByClass: Map<String, Set<String>>
    ): List<DiarioDateGroup> {
        val userTz = userTimeZone()
        val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = userTz }
        val displayFmt = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply { timeZone = userTz }

        val loggedIdsByDay = mutableMapOf<String, MutableSet<String>>()
        logs.forEach { log ->
            val studentId = log.student?.id ?: return@forEach
            val date = runCatching { isoFormatter.parse(log.date) }.getOrNull() ?: return@forEach
            val day = dayKeyFmt.format(date)
            loggedIdsByDay.getOrPut(day) { mutableSetOf() }.add(studentId)
        }

        val calTz = userTz
        val todayKey = dayKeyFmt.format(Calendar.getInstance(calTz).time)
        val yesterdayKey = dayKeyFmt.format(
            Calendar.getInstance(calTz).apply { add(Calendar.DAY_OF_MONTH, -1) }.time
        )

        return loggedIdsByDay.entries
            .sortedByDescending { it.key }
            .mapNotNull { (day, loggedIds) ->
                val turmasConcluidas = classes.mapNotNull { cls ->
                    val classStudentIds = studentsByClass[cls.id] ?: emptySet()
                    val logCount = classStudentIds.intersect(loggedIds).size
                    val status = ClassDiarioStatus(cls.id, cls.name, classStudentIds.size, logCount)
                    if (status.isDone) status else null
                }.sortedBy { it.className }
                if (turmasConcluidas.isEmpty()) return@mapNotNull null
                val parsedDay = runCatching { dayKeyFmt.parse(day) }.getOrNull()
                val label = when (day) {
                    todayKey -> "Hoje"
                    yesterdayKey -> "Ontem"
                    else -> parsedDay?.let { displayFmt.format(it) } ?: day
                }
                DiarioDateGroup(dateLabel = label, dateMs = parsedDay?.time ?: 0L, turmas = turmasConcluidas)
            }
    }

    private fun userTimeZone(): TimeZone = try {
        TimeZone.getTimeZone(TokenManager.getUserTimezone())
    } catch (_: Exception) {
        TimeZone.getDefault()
    }

    private fun todayRange(): Pair<String, String> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = isoFormatter.format(cal.time)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return from to isoFormatter.format(cal.time)
    }

    private fun historyStart(days: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(Calendar.DAY_OF_MONTH, -days)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return isoFormatter.format(cal.time)
    }
}
