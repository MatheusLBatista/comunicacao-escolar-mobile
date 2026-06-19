package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiTemplateField
import dev.fslab.comunicacao.escolar.model.CreateDailyLogRequest
import dev.fslab.comunicacao.escolar.model.DailyLogEntryRequest
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class StudentDailyLogState(
    val studentId: String,
    val studentName: String,
    val avatarUrl: String?,
    val existingLogId: String? = null,
    val isPresent: Boolean = false,
    val fieldValues: Map<String, String> = emptyMap(),
    val observation: String = ""
)

sealed class SubmitState {
    object Idle : SubmitState()
    object Submitting : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}

sealed class DiarioDeBordoUiState {
    object Loading : DiarioDeBordoUiState()
    data class Error(val message: String) : DiarioDeBordoUiState()
    data class Content(
        val classes: List<ApiClass>,
        val selectedClassId: String,
        val students: List<StudentDailyLogState>,
        val templateFields: List<ApiTemplateField>,
        val submitState: SubmitState = SubmitState.Idle
    ) : DiarioDeBordoUiState()
}

class DiarioDeBordoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DiarioDeBordoUiState>(DiarioDeBordoUiState.Loading)
    val uiState: StateFlow<DiarioDeBordoUiState> = _uiState.asStateFlow()

    private val teacherId: String
    private val schoolId: String

    private var allStudentsCache: List<dev.fslab.comunicacao.escolar.model.ApiSchoolUser> = emptyList()
    private var allClassesCache: List<ApiClass> = emptyList()
    private var templateId: String = ""
    private var templateFieldsCache: List<ApiTemplateField> = emptyList()
    private var existingLogIdByStudent: Map<String, String> = emptyMap()
    private var existingLogByStudent: Map<String, dev.fslab.comunicacao.escolar.model.DailyLogDoc> = emptyMap()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        val user = TokenManager.getSavedUser()
        teacherId = user?.id ?: ""
        schoolId = user?.schoolId ?: ""
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DiarioDeBordoUiState.Loading
            if (teacherId.isBlank() || schoolId.isBlank()) {
                _uiState.value = DiarioDeBordoUiState.Error("Dados do usuário não encontrados.")
                return@launch
            }
            try {
                val (dateFrom, dateTo) = todayRange()

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
                val templateDeferred = async {
                    RetrofitClient.adminApi.listTemplates(
                        mapOf("school_id" to schoolId, "limit" to "10")
                    )
                }
                val logsDeferred = async {
                    RetrofitClient.dailyLogsApi.getDailyLogsFiltered(
                        mapOf(
                            "school_id" to schoolId,
                            "teacher_id" to teacherId,
                            "date_from" to dateFrom,
                            "date_to" to dateTo
                        )
                    )
                }

                allClassesCache = classesDeferred.await().data?.docs.orEmpty()
                allStudentsCache = studentsDeferred.await().data?.docs.orEmpty()

                val template = templateDeferred.await().data?.docs?.firstOrNull()
                templateId = template?.id ?: ""
                templateFieldsCache = template?.fields ?: emptyList()

                val existingLogs = logsDeferred.await().data?.docs.orEmpty()
                existingLogIdByStudent = existingLogs
                    .filter { it.student?.id?.isNotBlank() == true }
                    .associate { it.student!!.id to it.id }
                existingLogByStudent = existingLogs
                    .filter { it.student?.id?.isNotBlank() == true }
                    .associateBy { it.student!!.id }

                if (allClassesCache.isEmpty()) {
                    _uiState.value = DiarioDeBordoUiState.Error("Nenhuma turma encontrada.")
                    return@launch
                }

                val selectedClassId = allClassesCache.first().id
                _uiState.value = DiarioDeBordoUiState.Content(
                    classes = allClassesCache,
                    selectedClassId = selectedClassId,
                    students = buildStudentStates(selectedClassId),
                    templateFields = templateFieldsCache
                )
            } catch (_: java.net.UnknownHostException) {
                _uiState.value = DiarioDeBordoUiState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _uiState.value = DiarioDeBordoUiState.Error("Tempo de conexão esgotado.")
            } catch (_: Exception) {
                _uiState.value = DiarioDeBordoUiState.Error("Erro ao carregar dados.")
            }
        }
    }

    fun selectClass(classId: String) {
        val state = _uiState.value as? DiarioDeBordoUiState.Content ?: return
        _uiState.value = state.copy(
            selectedClassId = classId,
            students = buildStudentStates(classId)
        )
    }

    fun togglePresence(studentId: String) {
        updateStudent(studentId) { it.copy(isPresent = !it.isPresent) }
    }

    fun updateField(studentId: String, fieldKey: String, value: String) {
        updateStudent(studentId) { s -> s.copy(fieldValues = s.fieldValues + (fieldKey to value)) }
    }

    fun updateObservation(studentId: String, text: String) {
        updateStudent(studentId) { it.copy(observation = text) }
    }

    fun resetSubmitState() {
        val state = _uiState.value as? DiarioDeBordoUiState.Content ?: return
        _uiState.value = state.copy(submitState = SubmitState.Idle)
    }

    fun submit() {
        val state = _uiState.value as? DiarioDeBordoUiState.Content ?: return
        if (templateId.isBlank()) {
            _uiState.value = state.copy(
                submitState = SubmitState.Error("Nenhum template de diário configurado para esta escola.")
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(submitState = SubmitState.Submitting)
            val now = nowIso()
            try {
                val currentState = _uiState.value as? DiarioDeBordoUiState.Content ?: return@launch
                coroutineScope {
                    val deferreds = currentState.students.map { student ->
                        async {
                            val request = CreateDailyLogRequest(
                                schoolId = schoolId,
                                studentId = student.studentId,
                                teacherId = teacherId,
                                dailyLogTemplateId = templateId,
                                date = now,
                                isPresent = student.isPresent,
                                entries = if (student.isPresent) {
                                    student.fieldValues
                                        .filter { it.value.isNotBlank() }
                                        .map { (key, value) -> DailyLogEntryRequest(key, value) }
                                } else emptyList(),
                                observation = student.observation
                            )
                            val logId = student.existingLogId
                            if (logId != null) {
                                RetrofitClient.dailyLogsApi.updateDailyLog(logId, request)
                            } else {
                                RetrofitClient.dailyLogsApi.createDailyLog(request)
                            }
                        }
                    }
                    deferreds.awaitAll()
                }
                val updated = _uiState.value as? DiarioDeBordoUiState.Content ?: currentState
                _uiState.value = updated.copy(submitState = SubmitState.Success)
            } catch (_: Exception) {
                val updated = _uiState.value as? DiarioDeBordoUiState.Content ?: state
                _uiState.value = updated.copy(
                    submitState = SubmitState.Error("Erro ao enviar o diário. Tente novamente.")
                )
            }
        }
    }

    private fun buildStudentStates(classId: String): List<StudentDailyLogState> =
        allStudentsCache
            .filter { user ->
                user.memberships.any { m -> m.role == "student" && m.classId == classId }
            }
            .sortedBy { it.fullName }
            .map { user ->
                val existingLog = existingLogByStudent[user.id]
                StudentDailyLogState(
                    studentId = user.id,
                    studentName = user.fullName,
                    avatarUrl = user.avatarUrl?.fixLocalhostUrl(),
                    existingLogId = existingLogIdByStudent[user.id],
                    isPresent = existingLog?.isPresent ?: false,
                    fieldValues = existingLog?.entries
                        ?.associate { it.fieldKey to it.value }
                        ?: emptyMap(),
                    observation = existingLog?.observation ?: ""
                )
            }

    private fun updateStudent(
        studentId: String,
        transform: (StudentDailyLogState) -> StudentDailyLogState
    ) {
        val state = _uiState.value as? DiarioDeBordoUiState.Content ?: return
        _uiState.value = state.copy(
            students = state.students.map {
                if (it.studentId == studentId) transform(it) else it
            }
        )
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
        val to = isoFormatter.format(cal.time)
        return from to to
    }

    private fun nowIso(): String = isoFormatter.format(Date())

    private fun String.fixLocalhostUrl(): String = replace("://localhost", "://34.194.113.240")
}