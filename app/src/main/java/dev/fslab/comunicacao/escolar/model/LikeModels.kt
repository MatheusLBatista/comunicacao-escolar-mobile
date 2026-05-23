package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class LikeData (
    @SerializedName("_id") val id: String? = null,
    @SerializedName("post_id") val postId: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class LikeResponse (
    @SerializedName("error") val error: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: LikeData?,
    @SerializedName("errors") val errors: List<String>? = emptyList()
)