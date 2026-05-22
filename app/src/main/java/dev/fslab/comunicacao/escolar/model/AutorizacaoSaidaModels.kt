package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class AutorizacaoSaida(
    val id: String,
    val studentName: String,
    val studentAvatarUrl: String?,
    val status: String,
    val autorizadoPor: String,
    val relacao: String,
    val horarioPrevisto: String
)

data class AutorizacoesSaidaResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: AutorizacoesSaidaData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class AutorizacoesSaidaData(
    @SerializedName("docs") val docs: List<AutorizacaoSaidaDoc> = emptyList(),
    @SerializedName("totalDocs") val totalDocs: Int = 0,
    @SerializedName("limit") val limit: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("page") val page: Int = 0
)

data class AutorizacaoSaidaDoc(
    @SerializedName("_id") val id: String = "",
    @SerializedName("student_id") val student: AutorizacaoSaidaStudent? = null,
    @SerializedName("status") val status: String = "pending",
    @SerializedName("authorized_person_name") val authorizedPersonName: String = "",
    @SerializedName("authorized_person_relation") val authorizedPersonRelation: String = "",
    @SerializedName("scheduled_time") val scheduledTime: String = ""
)

data class AutorizacaoSaidaStudent(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class CancelarAutorizacaoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}