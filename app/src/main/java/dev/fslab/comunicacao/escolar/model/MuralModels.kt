package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class MuralRequest (
    @SerializedName("id") val id:String
)

data class TargetInfo(
    @SerializedName("scope") val scope: String = "",
    @SerializedName("target_id") val target_id: String? = null
)

data class  Docs (
    @SerializedName("_id") val id:String = "",
    @SerializedName("school_id") val school_id:String ="",
    @SerializedName("author_id") val authorId: String = "",
    @SerializedName("title") val title:String = "",
    @SerializedName("content") val content:String ="",
    @SerializedName("target") val target: TargetInfo = TargetInfo(),
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("likes_count") val likesCount: Int? = 0,
    @SerializedName("user_liked") val userLiked: List<String>? = emptyList()
)


data class Dados (
    @SerializedName("docs") val docs: List<Docs> = emptyList(),
    @SerializedName("totalDocs") val totalDocs: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("pagingCounter") val pagingCounter: Int,
    @SerializedName("hasPrevPage") val hasPrevPage: Boolean,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("prevPage") val prevPage: Int? = null,
    @SerializedName("nextPage") val nextPage: Int? = null
)
data class MuralResponse (
    @SerializedName("error") val error: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: Dados
)