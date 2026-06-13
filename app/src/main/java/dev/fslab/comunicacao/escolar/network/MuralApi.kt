package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.CreatePostRequest
import dev.fslab.comunicacao.escolar.model.MuralRequest
import dev.fslab.comunicacao.escolar.model.MuralResponse
import dev.fslab.comunicacao.escolar.model.SinglePostResponse
import dev.fslab.comunicacao.escolar.model.DeletePostResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MuralApi {

    @GET("schools/{id}/posts")
    suspend fun getPosts(
        @Path("id") id: String,
        @Query("created_at") before: String? = null
    ): MuralResponse

    @POST("schools/{id}/posts")
    suspend fun createPost(
        @Path("id") schoolId: String,
        @Body request: CreatePostRequest
    ): SinglePostResponse

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: String): SinglePostResponse

    @Multipart
    @POST("posts/{id}/attachments")
    suspend fun uploadPostAttachments(
        @Path("id") postId: String,
        @Part files: List<MultipartBody.Part>,
        @Query("notify") notify: Boolean = false
    ): SinglePostResponse

    @GET("attachments/{id}")
    suspend fun getAttachment(@Path("id") id: String): ResponseBody

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): DeletePostResponse

}