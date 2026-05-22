package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class AutorizacaoSaida(
    val id: String,
    val studentName: String,
    val studentAvatarUrl: String?,
    val status: String,
    val autorizadoPor: String,
    val relacao: String,
    val validAte: String
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
    @SerializedName("authorized_person") val authorizedPerson: AutorizacaoAuthorizedPerson? = null,
    @SerializedName("authorized_by") val authorizedBy: AutorizacaoAuthorizedBy? = null,
    @SerializedName("valid_from") val validFrom: String = "",
    @SerializedName("valid_until") val validUntil: String = "",
    @SerializedName("used") val used: Boolean = false,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("qr_code") val qrCode: String = ""
)

data class AutorizacaoSaidaStudent(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class AutorizacaoAuthorizedPerson(
    @SerializedName("name") val name: String = "",
    @SerializedName("document") val document: String = "",
    @SerializedName("relationship") val relationship: String = "",
    @SerializedName("photo_url") val photoUrl: String? = null
)

data class AutorizacaoAuthorizedBy(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("email") val email: String = ""
)

data class CancelarAutorizacaoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}
