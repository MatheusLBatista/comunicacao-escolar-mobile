package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class MuralRequest (
    @SerializedName("id") val id:String
)

data class TargetInfo(
    @SerializedName("scope") val scope: String = "",
    @SerializedName("target_id") val target_id: String? = null
)

data class  MuralResponse (
    @SerializedName("_id") val id:String = "",
    @SerializedName("school_id") val school_id:String =" ",
    @SerializedName("title") val title:String = "",
    @SerializedName("content") val content:String ="",
    @SerializedName("target") val target: TargetInfo = TargetInfo(),
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)