package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.async
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

sealed class DiarioDeBordoListUiState {
    object Loading : DiarioDeBordoListUiState()
    data class Error(val message: String) : DiarioDeBordoListUiState()
    data class Content(
        val pendentes: List<ClassDiarioStatus>,
        val concluidas: List<ClassDiarioStatus>
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

                val classes = classesDeferred.await().data?.docs.orEmpty()
                val students = studentsDeferred.await().data?.docs.orEmpty()
                val logs = logsDeferred.await().data?.docs.orEmpty()
                val loggedStudentIds = logs.mapNotNull { it.student?.id }.toSet()

                val statuses = classes.map { cls ->
                    val classStudentIds = students
                        .filter { u -> u.memberships.any { m -> m.role == "student" && m.classId == cls.id } }
                        .map { it.id }
                        .toSet()
                    ClassDiarioStatus(
                        classId = cls.id,
                        className = cls.name,
                        studentCount = classStudentIds.size,
                        logCount = classStudentIds.intersect(loggedStudentIds).size
                    )
                }

                _uiState.value = DiarioDeBordoListUiState.Content(
                    pendentes = statuses.filter { !it.isDone },
                    concluidas = statuses.filter { it.isDone }
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
}
