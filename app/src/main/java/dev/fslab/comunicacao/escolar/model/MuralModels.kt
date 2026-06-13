package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class MuralRequest (
    @SerializedName("id") val id:String
)

data class CreatePostRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("target") val target: TargetInfo? = null,
    @SerializedName("wait_attachments") val waitAttachments: Boolean = false
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

data class SinglePostResponse (
    @SerializedName("error") val error: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String? = "",
    @SerializedName("data") val data: Docs
)

data class DeletePostResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String? = ""
)

data class AttachmentResponse(
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttachmentResponse
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
