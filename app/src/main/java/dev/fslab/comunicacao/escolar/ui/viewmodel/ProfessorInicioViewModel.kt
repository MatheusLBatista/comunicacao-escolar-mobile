package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.AlunoAdmin
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.model.Turma
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class AtividadeRecente(
    val actorName: String,
    val avatarUrl: String?,
    val studentName: String,
    val dateDisplay: String,
    val timeAgo: String
)

sealed class ProfessorInicioUiState {
    object Loading : ProfessorInicioUiState()
    object Empty : ProfessorInicioUiState()
    data class Error(val message: String) : ProfessorInicioUiState()
    data class Content(val atividades: List<AtividadeRecente>) : ProfessorInicioUiState()
}

class ProfessorInicioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfessorInicioUiState>(ProfessorInicioUiState.Loading)
    val uiState: StateFlow<ProfessorInicioUiState> = _uiState.asStateFlow()

    private val _turmasCount = MutableStateFlow<Int?>(null)
    val turmasCount: StateFlow<Int?> = _turmasCount.asStateFlow()

    private val _alunosCount = MutableStateFlow<Int?>(null)
    val alunosCount: StateFlow<Int?> = _alunosCount.asStateFlow()

    private val _turmas = MutableStateFlow<List<Turma>>(emptyList())
    val turmas: StateFlow<List<Turma>> = _turmas.asStateFlow()

    private val _selectedClassStudents = MutableStateFlow<List<AlunoAdmin>>(emptyList())
    val selectedClassStudents: StateFlow<List<AlunoAdmin>> = _selectedClassStudents.asStateFlow()

    private val _loadingClassStudents = MutableStateFlow(false)
    val loadingClassStudents: StateFlow<Boolean> = _loadingClassStudents.asStateFlow()

    private val _responsaveis = MutableStateFlow<List<ApiSchoolUser>>(emptyList())
    val responsaveis: StateFlow<List<ApiSchoolUser>> = _responsaveis.asStateFlow()

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR")).apply {
        timeZone = TimeZone.getDefault()
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfessorInicioUiState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _uiState.value = ProfessorInicioUiState.Error("Sessão expirada.")
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacoes(
                    token = "Bearer $token"
                )
                val docs = response.data?.docs.orEmpty().take(4)
                if (docs.isEmpty()) {
                    _uiState.value = ProfessorInicioUiState.Empty
                } else {
                    _uiState.value = ProfessorInicioUiState.Content(docs.map { it.toAtividade() })
                }
            } catch (_: java.net.UnknownHostException) {
                _uiState.value = ProfessorInicioUiState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _uiState.value = ProfessorInicioUiState.Error("Tempo de conexão esgotado.")
            } catch (_: Exception) {
                _uiState.value = ProfessorInicioUiState.Error("Erro ao carregar atividades recentes.")
            }
        }
    }

    private fun AutorizacaoSaidaDoc.toAtividade(): AtividadeRecente {
        val now = Date()
        val createdDate = runCatching { isoFormatter.parse(createdAt) }.getOrNull()

        val timeAgo = if (createdDate != null) {
            val diffMs = now.time - createdDate.time
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)
            when {
                minutes < 1  -> "agora"
                hours < 1    -> "${minutes}m atrás"
                hours < 24   -> "${hours}h atrás"
                else         -> "${days}d atrás"
            }
        } else ""

        val dateDisplay = if (createdDate != null) {
            val todayStr = dateFormatter.format(now)
            val createdStr = dateFormatter.format(createdDate)
            if (createdStr == todayStr) "hoje às ${timeFormatter.format(createdDate)}" else createdStr
        } else ""

        return AtividadeRecente(
            actorName = authorizedBy?.fullName ?: "Responsável",
            avatarUrl = authorizedBy?.avatarUrl?.takeIf { it.isNotBlank() }?.fixLocalhostUrl(),
            studentName = student?.fullName ?: "Aluno",
            dateDisplay = dateDisplay,
            timeAgo = timeAgo
        )
    }

    fun loadStats(schoolId: String, teacherId: String) {
        viewModelScope.launch {
            val classes = try {
                RetrofitClient.adminApi.listClasses(
                    schoolId,
                    mapOf("teacher_id" to teacherId, "limit" to "100")
                ).data?.docs.orEmpty()
            } catch (_: Exception) { emptyList() }

            _turmasCount.value = classes.size
            _turmas.value = classes.map { it.toTurma() }

            if (classes.isEmpty()) {
                _alunosCount.value = 0
                return@launch
            }

            val total = classes.sumOf { cls ->
                try {
                    RetrofitClient.adminApi.listUsers(
                        schoolId,
                        mapOf("role" to "student", "class_id" to cls.id, "limit" to "1")
                    ).data?.totalDocs ?: 0
                } catch (_: Exception) { 0 }
            }
            _alunosCount.value = total
        }
    }

    fun loadResponsaveis(schoolId: String) {
        viewModelScope.launch {
            try {
                val docs = RetrofitClient.adminApi.listUsers(
                    schoolId,
                    mapOf("role" to "parent", "limit" to "100")
                ).data?.docs.orEmpty()
                _responsaveis.value = docs
            } catch (_: Exception) { }
        }
    }

    fun loadStudentsForClass(schoolId: String, classId: String) {
        viewModelScope.launch {
            _loadingClassStudents.value = true
            _selectedClassStudents.value = emptyList()
            try {
                val docs = RetrofitClient.adminApi.listUsers(
                    schoolId,
                    mapOf("role" to "student", "class_id" to classId, "limit" to "100")
                ).data?.docs.orEmpty()
                _selectedClassStudents.value = docs.map { it.toAlunoAdmin(schoolId) }
            } catch (_: Exception) { }
            _loadingClassStudents.value = false
        }
    }

    private fun String.fixLocalhostUrl(): String =
        replace("://localhost", "://localhost")
}
