package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.UpdateUserRequest
import dev.fslab.comunicacao.escolar.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {

    @GET("users/{id}")
    suspend fun getById(@Path("id") id: String): UserResponse

    @PATCH("users/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): UserResponse

    @Multipart
    @PATCH("me/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): UserResponse

    @DELETE("me/avatar")
    suspend fun deleteAvatar(): UserResponse
}
