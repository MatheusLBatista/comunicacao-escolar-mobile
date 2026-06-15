package dev.fslab.comunicacao.escolar.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class AutorizacaoSaida(
    val id: String,
    val schoolId: String,
    val studentId: String,
    val studentName: String,
    val studentAvatarUrl: String?,
    val status: String,
    val autorizadoPor: String,
    val autorizadoDocumento: String,
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

data class AutorizacaoSaidaSchool(
    @SerializedName("_id") val id: String = ""
)

data class AutorizacaoSaidaDoc(
    @SerializedName("_id") val id: String = "",
    @SerializedName("school_id") val school: AutorizacaoSaidaSchool? = null,
    @SerializedName("student_id") val student: AutorizacaoSaidaStudent? = null,
    @SerializedName("authorized_person") val authorizedPerson: AutorizacaoAuthorizedPerson? = null,
    @SerializedName("authorized_by") val authorizedBy: AutorizacaoAuthorizedBy? = null,
    @SerializedName("valid_from") val validFrom: String = "",
    @SerializedName("valid_until") val validUntil: String = "",
    @SerializedName("used") val used: Boolean = false,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("qr_code") val qrCode: String = "",
    @SerializedName("created_at") val createdAt: String = ""
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
    @SerializedName("email") val email: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class PatchAutorizacaoRequest(
    @SerializedName("active") val active: Boolean
)

data class CancelarAutorizacaoResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class CreatePickupAuthorizationRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("authorized_by") val authorizedBy: String,
    @SerializedName("authorized_person") val authorizedPerson: CreateAuthorizedPerson,
    @SerializedName("qr_code") val qrCode: String,
    @SerializedName("valid_from") val validFrom: String,
    @SerializedName("valid_until") val validUntil: String,
    @SerializedName("used") val used: Boolean = false,
    @SerializedName("active") val active: Boolean = true
)

data class CreateAuthorizedPerson(
    @SerializedName("name") val name: String,
    @SerializedName("document") val document: String,
    @SerializedName("relationship") val relationship: String,
    @SerializedName("photo_url") val photoUrl: String? = null
)

data class CreatePickupAuthorizationResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList(),
    @SerializedName("data") val data: CreatedAuthorizationData? = null
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class CreatedAuthorizationData(
    @SerializedName("_id") val id: String = ""
)

data class AutorizacaoSaidaByIdResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList(),
    @SerializedName("data") val data: AutorizacaoSaidaDoc? = null
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class CreatePickupLogRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("authorization_id") val authorizationId: String? = null,
    @SerializedName("method") val method: String = "qr_code",
    @SerializedName("picked_up_by") val pickedUpBy: PickedUpBy,
    @SerializedName("verified_by") val verifiedBy: String,
    @SerializedName("departure_time") val departureTime: String,
    @SerializedName("notes") val notes: String = ""
)

data class PickedUpBy(
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("document") val document: String,
    @SerializedName("relationship") val relationship: String? = null
)

data class CreatePickupLogResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class PickupLogAuthorization(
    @SerializedName("_id") val id: String = ""
)

class PickupLogAuthorizationDeserializer : JsonDeserializer<PickupLogAuthorization> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): PickupLogAuthorization? {
        if (json.isJsonNull) return null
        return if (json.isJsonPrimitive) PickupLogAuthorization(id = json.asString)
        else PickupLogAuthorization(id = json.asJsonObject["_id"]?.asString ?: "")
    }
}

data class PickupLogDoc(
    @SerializedName("_id") val id: String = "",
    @SerializedName("student_id") val student: PickupLogStudent? = null,
    @SerializedName("picked_up_by") val pickedUpBy: PickupLogPickedUpByDoc? = null,
    @SerializedName("method") val method: String = "",
    @SerializedName("departure_time") val departureTime: String = "",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("verified_by") val verifiedBy: PickupLogVerifiedBy? = null,
    @SerializedName("authorization_id") val authorization: PickupLogAuthorization? = null
)

data class PickupLogStudent(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class PickupLogPickedUpByDoc(
    @SerializedName("name") val name: String = "",
    @SerializedName("document") val document: String = "",
    @SerializedName("relationship") val relationship: String? = null
)

data class PickupLogVerifiedBy(
    @SerializedName("_id") val id: String = "",
    @SerializedName("full_name") val fullName: String = ""
)

class PickupLogVerifiedByDeserializer : JsonDeserializer<PickupLogVerifiedBy> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): PickupLogVerifiedBy? {
        if (json.isJsonNull) return null
        return if (json.isJsonPrimitive) PickupLogVerifiedBy(id = json.asString, fullName = "")
        else {
            val obj = json.asJsonObject
            PickupLogVerifiedBy(
                id = obj["_id"]?.asString ?: "",
                fullName = obj["full_name"]?.asString ?: ""
            )
        }
    }
}

data class PickupLogsResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: PickupLogsData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class PickupLogsData(
    @SerializedName("docs") val docs: List<PickupLogDoc> = emptyList(),
    @SerializedName("totalDocs") val totalDocs: Int = 0,
    @SerializedName("limit") val limit: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("page") val page: Int = 0
)

data class PickupLogItem(
    val id: String,
    val studentName: String,
    val studentAvatarUrl: String?,
    val pickedUpName: String,
    val relationship: String,
    val time: String,
    val isManual: Boolean
)

data class PickupLogUi(
    val id: String,
    val authorizationId: String,
    val studentName: String,
    val pickedUpByName: String,
    val departureTime: String,
    val verifiedByName: String
)

data class PatchAutorizacaoUsedRequest(
    @SerializedName("used") val used: Boolean
)
