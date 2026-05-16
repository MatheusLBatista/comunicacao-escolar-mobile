package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.json.JSONObject
import dev.fslab.comunicacao.escolar.model.AdminStats
import dev.fslab.comunicacao.escolar.model.ApiAuditLog
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiDailyLogTemplate
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.AlunoAdmin
import dev.fslab.comunicacao.escolar.model.ApiStudentInput
import dev.fslab.comunicacao.escolar.model.CreateClassRequest
import dev.fslab.comunicacao.escolar.model.MoveStudentClassRequest
import dev.fslab.comunicacao.escolar.model.UpdateClassRequest
import dev.fslab.comunicacao.escolar.model.CreateTemplateFieldRequest
import dev.fslab.comunicacao.escolar.model.CreateTemplateRequest
import dev.fslab.comunicacao.escolar.model.LinkToSchoolRequest
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    companion object {
        private const val TAG = "AdminViewModel"
        private const val PAGE_LIMIT = "100"
    }

    // Turmas
    private val _turmas = MutableStateFlow<List<ApiClass>>(emptyList())
    val turmas: StateFlow<List<ApiClass>> = _turmas.asStateFlow()

    private val _turmasLoading = MutableStateFlow(false)
    val turmasLoading: StateFlow<Boolean> = _turmasLoading.asStateFlow()

    private val _turmasError = MutableStateFlow<String?>(null)
    val turmasError: StateFlow<String?> = _turmasError.asStateFlow()

    // Usuários
    private val _professores = MutableStateFlow<List<ApiSchoolUser>>(emptyList())
    val professores: StateFlow<List<ApiSchoolUser>> = _professores.asStateFlow()

    private val _responsaveis = MutableStateFlow<List<ApiSchoolUser>>(emptyList())
    val responsaveis: StateFlow<List<ApiSchoolUser>> = _responsaveis.asStateFlow()

    private val _usuariosLoading = MutableStateFlow(false)
    val usuariosLoading: StateFlow<Boolean> = _usuariosLoading.asStateFlow()

    // Alunos
    private val _alunos = MutableStateFlow<List<AlunoAdmin>>(emptyList())
    val alunos: StateFlow<List<AlunoAdmin>> = _alunos.asStateFlow()

    private val _alunosLoading = MutableStateFlow(false)
    val alunosLoading: StateFlow<Boolean> = _alunosLoading.asStateFlow()

    // Alunos sem turma (para picker em TurmaDetailScreen)
    private val _alunosSemTurma = MutableStateFlow<List<AlunoAdmin>>(emptyList())
    val alunosSemTurma: StateFlow<List<AlunoAdmin>> = _alunosSemTurma.asStateFlow()

    private val _alunosSemTurmaLoading = MutableStateFlow(false)
    val alunosSemTurmaLoading: StateFlow<Boolean> = _alunosSemTurmaLoading.asStateFlow()

    // Templates
    private val _templates = MutableStateFlow<List<ApiDailyLogTemplate>>(emptyList())
    val templates: StateFlow<List<ApiDailyLogTemplate>> = _templates.asStateFlow()

    private val _templatesLoading = MutableStateFlow(false)
    val templatesLoading: StateFlow<Boolean> = _templatesLoading.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<ApiAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<ApiAuditLog>> = _auditLogs.asStateFlow()

    private val _auditLogsLoading = MutableStateFlow(false)
    val auditLogsLoading: StateFlow<Boolean> = _auditLogsLoading.asStateFlow()

    // Stats
    private val _stats = MutableStateFlow<AdminStats?>(null)
    val stats: StateFlow<AdminStats?> = _stats.asStateFlow()

    // Action feedback
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    // Field-level errors (email field in VincularUsuarioScreen)
    private val _linkFieldError = MutableStateFlow<String?>(null)
    val linkFieldError: StateFlow<String?> = _linkFieldError.asStateFlow()

    // Turmas actions
    fun loadTurmas(schoolId: String) {
        viewModelScope.launch {
            _turmasLoading.value = true
            _turmasError.value = null
            try {
                val response = RetrofitClient.adminApi.listClasses(
                    schoolId,
                    mapOf("limit" to PAGE_LIMIT)
                )
                if (!response.error) {
                    _turmas.value = response.data?.docs ?: emptyList()
                } else {
                    _turmasError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _turmasError.value = e.localizedMessage ?: "Erro ao carregar turmas"
                Log.e(TAG, "loadTurmas error", e)
            } finally {
                _turmasLoading.value = false
            }
        }
    }

    fun assignTeacherToClass(schoolId: String, classId: String, teacherId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val cls = _turmas.value.find { it.id == classId } ?: return@launch
                val currentIds = cls.teachers.map { it.id }
                if (teacherId in currentIds) { onSuccess(); return@launch }
                val response = RetrofitClient.adminApi.updateClass(
                    schoolId, classId,
                    UpdateClassRequest(teacherIds = currentIds + teacherId)
                )
                if (!response.error && response.data != null) {
                    _turmas.value = _turmas.value.map { if (it.id == classId) response.data else it }
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao vincular turma. Tente novamente.",
                    on404 = "Turma ou professor não encontrado.",
                    on409 = "Este professor já está vinculado a essa turma."
                )
                Log.e(TAG, "assignTeacherToClass error", e)
            }
        }
    }

    fun removeTeacherFromClass(schoolId: String, classId: String, teacherId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val cls = _turmas.value.find { it.id == classId } ?: return@launch
                val newIds = cls.teachers.map { it.id }.filter { it != teacherId }
                val response = RetrofitClient.adminApi.updateClass(
                    schoolId, classId,
                    UpdateClassRequest(teacherIds = newIds)
                )
                if (!response.error && response.data != null) {
                    _turmas.value = _turmas.value.map { if (it.id == classId) response.data else it }
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao remover professor da turma. Tente novamente.",
                    on404 = "Turma não encontrada."
                )
                Log.e(TAG, "removeTeacherFromClass error", e)
            }
        }
    }

    fun createTurma(schoolId: String, name: String, shift: String, year: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.createClass(
                    schoolId,
                    CreateClassRequest(name = name, shift = shift, year = year)
                )
                if (!response.error && response.data != null) {
                    _turmas.value = _turmas.value + response.data
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao criar turma. Tente novamente.",
                    on409 = "Já existe uma turma com esse nome."
                )
                Log.e(TAG, "createTurma error", e)
            }
        }
    }

    // Usuários actions
    fun loadUsuarios(schoolId: String) {
        viewModelScope.launch {
            _usuariosLoading.value = true
            try {
                val queryBase = mapOf("limit" to PAGE_LIMIT)
                val profDeferred = async {
                    RetrofitClient.adminApi.listUsers(schoolId, queryBase + ("role" to "teacher"))
                }
                val respDeferred = async {
                    RetrofitClient.adminApi.listUsers(schoolId, queryBase + ("role" to "parent"))
                }
                val profResponse = profDeferred.await()
                val respResponse = respDeferred.await()

                if (!profResponse.error) _professores.value = profResponse.data?.docs ?: emptyList()
                if (!respResponse.error) _responsaveis.value = respResponse.data?.docs ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "loadUsuarios error", e)
            } finally {
                _usuariosLoading.value = false
            }
        }
    }

    fun loadAlunos(schoolId: String, classId: String? = null) {
        viewModelScope.launch {
            _alunosLoading.value = true
            try {
                val query = buildMap<String, String> {
                    put("limit", PAGE_LIMIT)
                    put("role", "student")
                    if (classId != null) put("class_id", classId)
                }
                val response = RetrofitClient.adminApi.listUsers(schoolId, query)
                if (!response.error) {
                    _alunos.value = (response.data?.docs ?: emptyList()).map { it.toAlunoAdmin(schoolId) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAlunos error", e)
            } finally {
                _alunosLoading.value = false
            }
        }
    }

    fun linkToSchool(
        schoolId: String,
        email: String,
        role: String,
        studentName: String? = null,
        classId: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val student = if (role == "parent" && studentName != null && classId != null) {
                    ApiStudentInput(fullName = studentName, classId = classId)
                } else null

                val response = RetrofitClient.adminApi.linkToSchool(
                    schoolId,
                    LinkToSchoolRequest(email = email, role = role, student = student)
                )
                if (!response.error) {
                    if (role == "teacher" && classId != null) {
                        val userId = response.data?.id?.takeIf { it.isNotBlank() }
                        if (userId != null) {
                            try {
                                val classesResp = RetrofitClient.adminApi.listClasses(schoolId, mapOf("limit" to PAGE_LIMIT))
                                val freshClass = classesResp.data?.docs?.find { it.id == classId }
                                    ?: _turmas.value.find { it.id == classId }
                                if (freshClass != null) {
                                    val currentIds = freshClass.teachers.map { it.id }
                                    if (userId !in currentIds) {
                                        val updateResp = RetrofitClient.adminApi.updateClass(
                                            schoolId, classId,
                                            UpdateClassRequest(teacherIds = currentIds + userId)
                                        )
                                        if (updateResp.data != null) {
                                            _turmas.value = _turmas.value.map { if (it.id == classId) updateResp.data else it }
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                                // Silencia falha de atribuição — vínculo principal teve sucesso
                            }
                        }
                    }
                    _actionSuccess.value = "Usuário vinculado com sucesso!"
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    409 -> _linkFieldError.value = "Este usuário já está vinculado à escola."
                    else -> _actionError.value = e.toFriendlyMessage()
                }
                Log.e(TAG, "linkToSchool HTTP error", e)
            } catch (e: Exception) {
                _actionError.value = "Erro ao vincular usuário. Verifique sua conexão."
                Log.e(TAG, "linkToSchool error", e)
            }
        }
    }

    // Filhos actions
    fun addStudentToParent(
        schoolId: String,
        userId: String,
        fullName: String,
        classId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.addStudentToParent(
                    schoolId, userId,
                    ApiStudentInput(fullName = fullName, classId = classId)
                )
                if (!response.error) {
                    loadUsuarios(schoolId)
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao adicionar aluno. Tente novamente.",
                    on404 = "Responsável ou turma não encontrado.",
                    on409 = "Este aluno já está vinculado a esse responsável."
                )
                Log.e(TAG, "addStudentToParent error", e)
            }
        }
    }

    fun removeStudentFromParent(
        schoolId: String,
        userId: String,
        studentId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.removeStudentFromParent(schoolId, userId, studentId)
                if (!response.error) {
                    loadUsuarios(schoolId)
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao remover aluno. Tente novamente.",
                    on404 = "Aluno não encontrado."
                )
                Log.e(TAG, "removeStudentFromParent error", e)
            }
        }
    }

    fun loadAlunosSemTurma(schoolId: String) {
        viewModelScope.launch {
            _alunosSemTurmaLoading.value = true
            try {
                val response = RetrofitClient.adminApi.listUsers(
                    schoolId,
                    mapOf("limit" to PAGE_LIMIT, "role" to "student")
                )
                if (!response.error) {
                    _alunosSemTurma.value = (response.data?.docs ?: emptyList())
                        .map { it.toAlunoAdmin(schoolId) }
                        .filter { it.classId == null }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAlunosSemTurma error", e)
            } finally {
                _alunosSemTurmaLoading.value = false
            }
        }
    }

    fun assignStudentToClass(schoolId: String, studentId: String, classId: String, onSuccess: () -> Unit) {
        val parent = _responsaveis.value.find { resp ->
            resp.memberships.any { m -> m.role == "parent" && m.associatedStudents.any { s -> s.id == studentId } }
        }
        if (parent == null) {
            _actionError.value = "Responsável do aluno não encontrado. Recarregue a página e tente novamente."
            return
        }
        moveStudentToClass(schoolId, parent.id, studentId, classId, onSuccess)
    }

    fun moveStudentToClass(
        schoolId: String,
        parentId: String,
        studentId: String,
        classId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.moveStudentToClass(
                    schoolId, parentId, studentId, MoveStudentClassRequest(classId = classId)
                )
                if (!response.error) {
                    loadAlunos(schoolId, classId)
                    loadAlunosSemTurma(schoolId)
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao mover aluno de turma. Tente novamente.",
                    on404 = "Aluno ou turma não encontrado."
                )
                Log.e(TAG, "moveStudentToClass error", e)
            }
        }
    }

    // Templates actions
    fun loadTemplates(schoolId: String) {
        viewModelScope.launch {
            _templatesLoading.value = true
            try {
                val response = RetrofitClient.adminApi.listTemplates(
                    mapOf("school_id" to schoolId, "limit" to PAGE_LIMIT)
                )
                if (!response.error) {
                    _templates.value = response.data?.docs ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadTemplates error", e)
            } finally {
                _templatesLoading.value = false
            }
        }
    }

    fun createTemplate(
        schoolId: String,
        nome: String,
        fields: List<CreateTemplateFieldRequest> = emptyList(),
        onSuccess: (ApiDailyLogTemplate) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.createTemplate(
                    CreateTemplateRequest(
                        schoolId = schoolId,
                        name = nome.trim(),
                        fields = fields
                    )
                )
                if (!response.error && response.data != null) {
                    _templates.value = _templates.value + response.data
                    onSuccess(response.data)
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao criar template. Tente novamente.",
                    on409 = "Já existe um template com esse nome."
                )
                Log.e(TAG, "createTemplate error", e)
            }
        }
    }

    // Audit Logs actions
    fun loadAuditLogs(schoolId: String) {
        viewModelScope.launch {
            _auditLogsLoading.value = true
            try {
                val response = RetrofitClient.adminApi.listAuditLogs(
                    schoolId,
                    mapOf("limit" to PAGE_LIMIT)
                )
                if (!response.error) {
                    _auditLogs.value = response.data?.docs ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAuditLogs error", e)
            } finally {
                _auditLogsLoading.value = false
            }
        }
    }

    // Dashboard stats
    fun loadDashboardStats(schoolId: String) {
        viewModelScope.launch {
            try {
                val query = mapOf("limit" to "1")
                val profDeferred = async {
                    RetrofitClient.adminApi.listUsers(schoolId, query + ("role" to "teacher"))
                }
                val respDeferred = async {
                    RetrofitClient.adminApi.listUsers(schoolId, query + ("role" to "parent"))
                }
                val alunoDeferred = async {
                    RetrofitClient.adminApi.listUsers(schoolId, query + ("role" to "student"))
                }
                val classDeferred = async {
                    RetrofitClient.adminApi.listClasses(schoolId, query)
                }
                val profResp = profDeferred.await()
                val respResp = respDeferred.await()
                val alunoResp = alunoDeferred.await()
                val classResp = classDeferred.await()

                _stats.value = AdminStats(
                    totalProfessores = profResp.data?.totalDocs ?: 0,
                    totalResponsaveis = respResp.data?.totalDocs ?: 0,
                    totalAlunos = alunoResp.data?.totalDocs ?: 0,
                    totalTurmas = classResp.data?.totalDocs ?: 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadDashboardStats error", e)
            }
        }
    }

    fun deactivateResponsavel(schoolId: String, responsavelId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val studentIds = _responsaveis.value
                    .find { it.id == responsavelId }
                    ?.memberships
                    ?.filter { it.role == "parent" }
                    ?.flatMap { it.associatedStudents }
                    ?.map { it.id }
                    ?: emptyList()

                for (studentId in studentIds) {
                    RetrofitClient.adminApi.deactivateMembership(schoolId, studentId)
                }

                val response = RetrofitClient.adminApi.deactivateMembership(schoolId, responsavelId)
                if (!response.error) {
                    loadUsuarios(schoolId)
                    loadTurmas(schoolId)
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao desvincular responsável. Tente novamente.",
                    on404 = "Responsável não encontrado nesta escola."
                )
                Log.e(TAG, "deactivateResponsavel error", e)
            }
        }
    }

    fun deactivateMembership(schoolId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.deactivateMembership(schoolId, userId)
                if (!response.error) {
                    loadUsuarios(schoolId)
                    loadTurmas(schoolId)
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao desvincular usuário. Tente novamente.",
                    on404 = "Usuário não encontrado nesta escola."
                )
                Log.e(TAG, "deactivateMembership error", e)
            }
        }
    }

    fun activateMembership(schoolId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.adminApi.activateMembership(schoolId, userId)
                if (!response.error) {
                    loadUsuarios(schoolId)
                    loadTurmas(schoolId)
                    onSuccess()
                } else {
                    _actionError.value = response.getErrorMessage()
                }
            } catch (e: Exception) {
                _actionError.value = e.toFriendlyMessage(
                    fallback = "Erro ao reativar usuário. Tente novamente.",
                    on404 = "Usuário não encontrado nesta escola."
                )
                Log.e(TAG, "activateMembership error", e)
            }
        }
    }

    // Clear feedback
    fun clearActionError() { _actionError.value = null }
    fun clearActionSuccess() { _actionSuccess.value = null }
    fun clearLinkFieldError() { _linkFieldError.value = null }

    private fun retrofit2.HttpException.toFriendlyMessage(): String {
        if (code() == 400 || code() == 422) {
            try {
                val body = response()?.errorBody()?.string()
                if (!body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val arr = json.optJSONArray("errors")
                    if (arr != null && arr.length() > 0) {
                        val msg = arr.getJSONObject(0).optString("message")
                        if (msg.isNotBlank()) return msg
                    }
                    val msg = json.optString("message")
                    if (msg.isNotBlank()) return msg
                }
            } catch (_: Exception) {}
        }
        return when (code()) {
            400 -> "Dados inválidos. Verifique as informações e tente novamente."
            401 -> "Sessão expirada. Faça login novamente."
            403 -> "Você não tem permissão para esta ação."
            404 -> "Registro não encontrado."
            409 -> "Este registro já existe."
            500, 502, 503 -> "Erro no servidor. Tente novamente em instantes."
            else -> "Erro inesperado. Tente novamente."
        }
    }

    private fun Exception.toFriendlyMessage(
        fallback: String,
        on404: String? = null,
        on409: String? = null
    ): String = if (this is retrofit2.HttpException) when (code()) {
        404 -> on404 ?: toFriendlyMessage()
        409 -> on409 ?: toFriendlyMessage()
        else -> toFriendlyMessage()
    } else fallback
}
