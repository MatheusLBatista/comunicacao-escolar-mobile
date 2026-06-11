package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaDoc
import dev.fslab.comunicacao.escolar.model.CreatePickupLogRequest
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoRequest
import dev.fslab.comunicacao.escolar.model.PickedUpBy
import dev.fslab.comunicacao.escolar.model.PickupLogDoc
import dev.fslab.comunicacao.escolar.model.PickupLogItem
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AutorizacaoProfessorItem(
    val doc: AutorizacaoSaidaDoc,
    val studentName: String,
    val studentAvatarUrl: String?,
    val responsavelName: String,
    val autorizadoPorName: String,
    val autorizadoPorRelacao: String,
    val horarioPrevisto: String
)

sealed class ProfAutorizacoesState {
    object Loading : ProfAutorizacoesState()
    data class Error(val message: String) : ProfAutorizacoesState()
    object Empty : ProfAutorizacoesState()
    data class Content(val items: List<AutorizacaoProfessorItem>) : ProfAutorizacoesState()
}

sealed class ProfRegistradasState {
    object Loading : ProfRegistradasState()
    data class Error(val message: String) : ProfRegistradasState()
    object Empty : ProfRegistradasState()
    data class Content(val items: List<PickupLogItem>) : ProfRegistradasState()
}

data class RegistrarSaidaSheetState(
    val isVisible: Boolean = false,
    val step: Int = 1,
    val turmas: List<ApiClass> = emptyList(),
    val turmasLoading: Boolean = false,
    val turmaStudentCounts: Map<String, Int> = emptyMap(),
    val selectedTurma: ApiClass? = null,
    val allStudents: List<ApiSchoolUser> = emptyList(),
    val selectedStudent: ApiSchoolUser? = null,
    val searchQuery: String = "",
    val registrando: Boolean = false,
    val erro: String? = null
)

class ControleSaidaProfessorViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _autorizacoesState = MutableStateFlow<ProfAutorizacoesState>(ProfAutorizacoesState.Loading)
    val autorizacoesState: StateFlow<ProfAutorizacoesState> = _autorizacoesState.asStateFlow()

    private val _registradasState = MutableStateFlow<ProfRegistradasState>(ProfRegistradasState.Loading)
    val registradasState: StateFlow<ProfRegistradasState> = _registradasState.asStateFlow()

    private val _recusadasState = MutableStateFlow<ProfAutorizacoesState>(ProfAutorizacoesState.Loading)
    val recusadasState: StateFlow<ProfAutorizacoesState> = _recusadasState.asStateFlow()

    private val _confirmando = MutableStateFlow<String?>(null)
    val confirmando: StateFlow<String?> = _confirmando.asStateFlow()

    private val _recusando = MutableStateFlow<String?>(null)
    val recusando: StateFlow<String?> = _recusando.asStateFlow()

    private val _sheetState = MutableStateFlow(RegistrarSaidaSheetState())
    val sheetState: StateFlow<RegistrarSaidaSheetState> = _sheetState.asStateFlow()

    private val teacherId: String
    private val schoolId: String

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        val user = TokenManager.getSavedUser()
        teacherId = user?.id ?: ""
        schoolId = user?.schoolId ?: ""
        loadAll()
    }

    fun selectTab(tab: Int) { _selectedTab.value = tab }

    fun loadAll() {
        loadAutorizacoes()
        loadRegistradas()
        loadRecusadas()
    }

    fun loadAutorizacoes() {
        viewModelScope.launch {
            _autorizacoesState.value = ProfAutorizacoesState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _autorizacoesState.value = ProfAutorizacoesState.Error("Sessão expirada.")
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacoes(
                    token = "Bearer $token",
                    schoolId = schoolId.takeIf { it.isNotBlank() },
                    active = true,
                    used = false
                )
                if (response.error) {
                    _autorizacoesState.value = ProfAutorizacoesState.Error(response.getErrorMessage())
                    return@launch
                }
                val items = response.data?.docs.orEmpty().map { it.toUi() }
                _autorizacoesState.value = if (items.isEmpty()) ProfAutorizacoesState.Empty
                                           else ProfAutorizacoesState.Content(items)
            } catch (e: retrofit2.HttpException) {
                _autorizacoesState.value = ProfAutorizacoesState.Error("Erro ao carregar autorizações (${e.code()}).")
            } catch (_: java.net.UnknownHostException) {
                _autorizacoesState.value = ProfAutorizacoesState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _autorizacoesState.value = ProfAutorizacoesState.Error("Tempo de conexão esgotado.")
            } catch (_: Exception) {
                _autorizacoesState.value = ProfAutorizacoesState.Error("Erro ao carregar autorizações.")
            }
        }
    }

    fun loadRegistradas() {
        viewModelScope.launch {
            _registradasState.value = ProfRegistradasState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _registradasState.value = ProfRegistradasState.Error("Sessão expirada.")
                return@launch
            }
            val sid = schoolId.takeIf { it.isNotBlank() } ?: run {
                _registradasState.value = ProfRegistradasState.Error("Escola não encontrada.")
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.getPickupLogs(
                    token = "Bearer $token",
                    schoolId = sid
                )
                if (response.error) {
                    _registradasState.value = ProfRegistradasState.Error(response.getErrorMessage())
                    return@launch
                }
                val items = response.data?.docs.orEmpty().map { it.toPickupLogItem() }
                _registradasState.value = if (items.isEmpty()) ProfRegistradasState.Empty
                                         else ProfRegistradasState.Content(items)
            } catch (e: retrofit2.HttpException) {
                val detail = runCatching {
                    val body = e.response()?.errorBody()?.string() ?: ""
                    Regex(""""message"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
                }.getOrNull()
                _registradasState.value = ProfRegistradasState.Error(
                    detail ?: "Erro ao carregar registros (${e.code()})."
                )
            } catch (_: java.net.UnknownHostException) {
                _registradasState.value = ProfRegistradasState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _registradasState.value = ProfRegistradasState.Error("Tempo de conexão esgotado.")
            } catch (e: Exception) {
                _registradasState.value = ProfRegistradasState.Error("Erro ao carregar registros: ${e.message}")
            }
        }
    }

    fun loadRecusadas() {
        viewModelScope.launch {
            _recusadasState.value = ProfAutorizacoesState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _recusadasState.value = ProfAutorizacoesState.Error("Sessão expirada.")
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.getAutorizacoes(
                    token = "Bearer $token",
                    schoolId = schoolId.takeIf { it.isNotBlank() },
                    active = false
                )
                if (response.error) {
                    _recusadasState.value = ProfAutorizacoesState.Error(response.getErrorMessage())
                    return@launch
                }
                val items = response.data?.docs.orEmpty().map { it.toUi() }
                _recusadasState.value = if (items.isEmpty()) ProfAutorizacoesState.Empty
                                       else ProfAutorizacoesState.Content(items)
            } catch (e: retrofit2.HttpException) {
                _recusadasState.value = ProfAutorizacoesState.Error("Erro ao carregar recusadas (${e.code()}).")
            } catch (_: java.net.UnknownHostException) {
                _recusadasState.value = ProfAutorizacoesState.Error("Sem conexão com a internet.")
            } catch (_: java.net.SocketTimeoutException) {
                _recusadasState.value = ProfAutorizacoesState.Error("Tempo de conexão esgotado.")
            } catch (_: Exception) {
                _recusadasState.value = ProfAutorizacoesState.Error("Erro ao carregar recusadas.")
            }
        }
    }

    fun confirmarAutorizacao(item: AutorizacaoProfessorItem) {
        viewModelScope.launch {
            _confirmando.value = item.doc.id
            val token = TokenManager.getAccessToken() ?: run {
                _confirmando.value = null
                return@launch
            }
            try {
                val request = CreatePickupLogRequest(
                    schoolId = schoolId,
                    studentId = item.doc.student?.id ?: "",
                    authorizationId = item.doc.id,
                    method = "qr_code",
                    pickedUpBy = PickedUpBy(
                        name = item.doc.authorizedPerson?.name ?: "",
                        document = item.doc.authorizedPerson?.document ?: "",
                        relationship = item.doc.authorizedPerson?.relationship
                    ),
                    verifiedBy = teacherId,
                    departureTime = isoFormatter.format(Date())
                )
                val response = RetrofitClient.autorizacaoSaidaApi.criarPickupLog("Bearer $token", request)
                if (!response.error) {
                    loadAutorizacoes()
                    loadRegistradas()
                }
            } catch (_: Exception) {
            } finally {
                _confirmando.value = null
            }
        }
    }

    fun recusarAutorizacao(id: String) {
        viewModelScope.launch {
            _recusando.value = id
            val token = TokenManager.getAccessToken() ?: run {
                _recusando.value = null
                return@launch
            }
            try {
                val response = RetrofitClient.autorizacaoSaidaApi.cancelarAutorizacao(
                    "Bearer $token", id, PatchAutorizacaoRequest(active = false)
                )
                if (response.isSuccessful) {
                    loadAutorizacoes()
                    loadRecusadas()
                }
            } catch (_: Exception) {
            } finally {
                _recusando.value = null
            }
        }
    }

    fun abrirRegistrarSheet() {
        _sheetState.value = RegistrarSaidaSheetState(isVisible = true, turmasLoading = true)
        loadTurmasEAlunos()
    }

    fun fecharRegistrarSheet() {
        _sheetState.value = RegistrarSaidaSheetState(isVisible = false)
    }

    private fun loadTurmasEAlunos() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val classesDeferred = async {
                        RetrofitClient.adminApi.listClasses(
                            schoolId,
                            mapOf("teacher_id" to teacherId, "limit" to "100")
                        )
                    }
                    val usersDeferred = async {
                        RetrofitClient.adminApi.listUsers(
                            schoolId,
                            mapOf("role" to "student", "limit" to "100")
                        )
                    }
                    val classes = classesDeferred.await().data?.docs.orEmpty()
                    val students = usersDeferred.await().data?.docs.orEmpty()
                    val counts = classes.associate { cls ->
                        cls.id to students.count { user ->
                            user.memberships.any { m -> m.role == "student" && m.classId == cls.id }
                        }
                    }
                    _sheetState.value = _sheetState.value.copy(
                        turmasLoading = false,
                        turmas = classes,
                        turmaStudentCounts = counts,
                        allStudents = students
                    )
                }
            } catch (_: Exception) {
                _sheetState.value = _sheetState.value.copy(turmasLoading = false)
            }
        }
    }

    fun selectTurma(turma: ApiClass) {
        _sheetState.value = _sheetState.value.copy(
            step = 2,
            selectedTurma = turma,
            selectedStudent = null,
            searchQuery = ""
        )
    }

    fun goBackToStep1() {
        _sheetState.value = _sheetState.value.copy(
            step = 1,
            selectedTurma = null,
            selectedStudent = null,
            searchQuery = ""
        )
    }

    fun selectStudent(student: ApiSchoolUser) {
        val current = _sheetState.value.selectedStudent
        _sheetState.value = _sheetState.value.copy(
            selectedStudent = if (current?.id == student.id) null else student
        )
    }

    fun updateSearchQuery(query: String) {
        _sheetState.value = _sheetState.value.copy(searchQuery = query)
    }

    fun registrarSaidaManual(quemBuscou: String, documento: String, relacao: String) {
        val sheet = _sheetState.value
        val student = sheet.selectedStudent ?: return
        viewModelScope.launch {
            _sheetState.value = sheet.copy(registrando = true, erro = null)
            val token = TokenManager.getAccessToken() ?: run {
                _sheetState.value = _sheetState.value.copy(registrando = false, erro = "Sessão expirada.")
                return@launch
            }
            try {
                val request = CreatePickupLogRequest(
                    schoolId = schoolId,
                    studentId = student.id,
                    authorizationId = null,
                    method = "manual",
                    pickedUpBy = PickedUpBy(
                        name = quemBuscou.trim(),
                        document = documento.trim()
                    ),
                    verifiedBy = teacherId,
                    departureTime = isoFormatter.format(Date()),
                    notes = relacao.trim()
                )
                val response = RetrofitClient.autorizacaoSaidaApi.criarPickupLog("Bearer $token", request)
                if (response.error) {
                    _sheetState.value = _sheetState.value.copy(registrando = false, erro = response.getErrorMessage())
                } else {
                    _sheetState.value = RegistrarSaidaSheetState(isVisible = false)
                    loadRegistradas()
                }
            } catch (e: retrofit2.HttpException) {
                val detail = runCatching {
                    val body = e.response()?.errorBody()?.string() ?: ""
                    val msg = Regex(""""message"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
                    msg?.replaceFirstChar { it.uppercase() }
                }.getOrNull()
                _sheetState.value = _sheetState.value.copy(
                    registrando = false,
                    erro = detail ?: "Erro ao registrar saída (${e.code()})."
                )
            } catch (_: Exception) {
                _sheetState.value = _sheetState.value.copy(registrando = false, erro = "Erro ao registrar saída.")
            }
        }
    }

    private fun AutorizacaoSaidaDoc.toUi(): AutorizacaoProfessorItem {
        val time = runCatching { isoFormatter.parse(validFrom)?.let { timeFormatter.format(it) } }
            .getOrNull() ?: validFrom.take(5).ifBlank { "--:--" }
        return AutorizacaoProfessorItem(
            doc = this,
            studentName = student?.fullName?.trim()?.split(" ")?.firstOrNull() ?: "Aluno",
            studentAvatarUrl = student?.avatarUrl?.takeIf { it.isNotBlank() },
            responsavelName = authorizedBy?.fullName.orEmpty(),
            autorizadoPorName = authorizedPerson?.name.orEmpty(),
            autorizadoPorRelacao = authorizedPerson?.relationship.orEmpty(),
            horarioPrevisto = time
        )
    }

    private fun PickupLogDoc.toPickupLogItem(): PickupLogItem {
        val safeTime = departureTime.orEmpty()
        val time = runCatching { isoFormatter.parse(safeTime)?.let { timeFormatter.format(it) } }
            .getOrNull() ?: safeTime.take(5).ifBlank { "--:--" }
        return PickupLogItem(
            id = id.orEmpty(),
            studentName = student?.fullName?.trim()?.split(" ")?.firstOrNull().orEmpty(),
            studentAvatarUrl = student?.avatarUrl?.takeIf { it.isNotBlank() },
            pickedUpName = pickedUpBy?.name.orEmpty(),
            relationship = notes.orEmpty().ifBlank { pickedUpBy?.relationship.orEmpty() },
            time = time,
            isManual = method.orEmpty() == "manual"
        )
    }
}
