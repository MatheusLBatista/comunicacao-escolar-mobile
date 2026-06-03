package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName
import java.util.UUID
import dev.fslab.comunicacao.escolar.util.DateUtils

data class Turma(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val ano: Int = 2026,
    val shift: String = "",
    val professor: String? = null,
    val professorId: String? = null
)

data class ApiResponse<T>(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int = 200,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class PaginatedData<T>(
    @SerializedName("docs") val docs: List<T> = emptyList(),
    @SerializedName("totalDocs") val totalDocs: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("page") val page: Int = 1
)

data class ApiClass(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("shift") val shift: String,
    @SerializedName("year") val year: Int,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("teacher_ids") val teachers: List<ApiTeacherRef> = emptyList()
) {
    fun toTurma() = Turma(
        id = id,
        nome = name,
        ano = year,
        shift = shift,
        professor = teachers.firstOrNull()?.fullName,
        professorId = teachers.firstOrNull()?.id
    )
}

data class ApiTeacherRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email") val email: String? = null
)

data class CreateClassRequest(
    @SerializedName("name") val name: String,
    @SerializedName("shift") val shift: String,
    @SerializedName("year") val year: Int,
    @SerializedName("teacher_ids") val teacherIds: List<String> = emptyList()
)

data class UpdateClassRequest(
    @SerializedName("teacher_ids") val teacherIds: List<String>
)

data class ApiSchoolUser(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("memberships") val memberships: List<ApiMembership> = emptyList()
) {
    fun toProfessorAdmin() = ProfessorAdmin(id = id, nome = fullName, email = email ?: "", avatar = avatarUrl)
    fun toAlunoAdmin(schoolId: String = "") = AlunoAdmin(
        id = id,
        nome = fullName,
        email = email,
        avatar = avatarUrl,
        active = active,
        classId = memberships.find { (schoolId.isBlank() || it.schoolId == schoolId) && it.role == "student" }?.classId
    )
    fun toResponsavelAdmin(schoolId: String = "") = ResponsavelAdmin(
        id = id,
        nome = fullName,
        email = email ?: "",
        avatar = avatarUrl,
        filhos = memberships
            .filter { m -> (schoolId.isBlank() || m.schoolId == schoolId) && m.role == "parent" }
            .flatMap { m -> m.associatedStudents }
            .map { s -> FilhoAdmin(id = s.id, nome = s.fullName, classId = s.classId) }
    )
}

data class ApiDailyLogTemplate(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("fields") val fields: List<ApiTemplateField> = emptyList(),
    @SerializedName("ativo") val ativo: Boolean = true,
    @SerializedName("school_id") val schoolId: String? = null
) {
    fun toComunicadoTemplate() = ComunicadoTemplate(
        id = id,
        nome = name.ifBlank { fields.firstOrNull()?.label ?: "Diário de Bordo" },
        campos = fields.map { f ->
            CampoTemplate(
                id = f.id ?: UUID.randomUUID().toString(),
                nome = f.label,
                tipo = when (f.type) {
                    "select"  -> TipoCampo.SELECAO
                    "boolean" -> TipoCampo.SIM_NAO
                    else      -> TipoCampo.TEXTO_LIVRE
                },
                opcoes = f.options
            )
        }
    )
}

data class ApiTemplateField(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("key") val key: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("type") val type: String = "text",
    @SerializedName("options") val options: List<String> = emptyList()
)

data class CreateTemplateRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("name") val name: String,
    @SerializedName("fields") val fields: List<CreateTemplateFieldRequest> = emptyList()
)

data class UpdateTemplateRequest(
    @SerializedName("fields") val fields: List<CreateTemplateFieldRequest>
)

data class CreateTemplateFieldRequest(
    @SerializedName("key") val key: String,
    @SerializedName("label") val label: String,
    @SerializedName("type") val type: String = "text",
    @SerializedName("options") val options: List<String> = emptyList()
)

// API Audit Log
data class ApiAuditLog(
    @SerializedName("_id") val id: String = "",
    @SerializedName("user_id") val userId: Any? = null,
    @SerializedName("user_role") val userRole: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("resource_type") val resourceType: String = "",
    @SerializedName("resource_summary") val resourceSummary: String = "",
    @SerializedName("student_id") val studentId: Any? = null,
    @SerializedName("device_info") val deviceInfo: ApiDeviceInfo? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    private fun extractName(field: Any?): String? =
        (field as? Map<*, *>)?.get("full_name")?.toString()

    fun toAuditLog() = AuditLog(
        id = id,
        atorNome = extractName(userId) ?: "Usuário",
        tipoAtor = when (userRole) {
            "admin"   -> TipoAtor.ADMIN
            "teacher" -> TipoAtor.PROFESSOR
            else      -> TipoAtor.RESPONSAVEL
        },
        acao = when (action) {
            "create" -> "criou"
            "update" -> "atualizou"
            "delete" -> "removeu"
            "view"   -> "visualizou"
            else     -> action
        } + " " + when (resourceType) {
            "user"            -> "Usuário"
            "template"        -> "Template"
            "daily_log"       -> "Diário"
            "announcement"    -> "Comunicado"
            "message"         -> "Mensagem"
            "conversation"    -> "Conversa"
            "pickup_log"      -> "Saída"
            "student_profile" -> "Perfil do Aluno"
            else              -> resourceType.replace("_", " ")
        },
        destino = resourceSummary.ifBlank { resourceType.replace("_", " ") },
        aluno = extractName(studentId),
        dispositivo = deviceInfo?.platform?.takeIf { it.isNotBlank() && it != "web" },
        dataHora = DateUtils.getAuditFormat(createdAt)
    )
}

data class ApiDeviceInfo(
    @SerializedName("platform") val platform: String = ""
)

data class LinkToSchoolRequest(
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("student") val student: ApiStudentInput? = null
)

data class ApiStudentInput(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("class_id") val classId: String
)

data class MoveStudentClassRequest(
    @SerializedName("class_id") val classId: String
)

data class ComunicadoTemplate(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val campos: List<CampoTemplate> = emptyList()
)

data class CampoTemplate(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val tipo: TipoCampo = TipoCampo.SELECAO,
    val opcoes: List<String> = emptyList()
)

enum class TipoCampo(val label: String) {
    SELECAO("Seleção"),
    TEXTO_LIVRE("Texto livre"),
    SIM_NAO("Sim / Não")
}

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val atorNome: String,
    val tipoAtor: TipoAtor,
    val acao: String,
    val destino: String,
    val aluno: String? = null,
    val dispositivo: String? = null,
    val dataHora: String
)

enum class TipoAtor { RESPONSAVEL, PROFESSOR, ADMIN }

data class AdminStats(
    val totalProfessores: Int,
    val totalResponsaveis: Int,
    val totalAlunos: Int,
    val totalTurmas: Int
)

data class ProfessorAdmin(
    val id: String,
    val nome: String,
    val email: String,
    val avatar: String? = null,
    val turmas: List<Turma> = emptyList()
)

data class ResponsavelAdmin(
    val id: String,
    val nome: String,
    val email: String,
    val avatar: String? = null,
    val filhos: List<FilhoAdmin> = emptyList()
)

data class FilhoAdmin(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val turma: String? = null,
    val classId: String? = null
)

data class AlunoAdmin(
    val id: String,
    val nome: String,
    val email: String? = null,
    val avatar: String? = null,
    val active: Boolean = true,
    val classId: String? = null
)
