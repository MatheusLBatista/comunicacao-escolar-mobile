package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class Like (
    @SerializedName("post_id") val postId: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("_id") val id:String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

data class LikeResponse (
    @SerializedName("error") val error: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: Like,
    @SerializedName("errors") val errors: List<String>? = emptyList()
)