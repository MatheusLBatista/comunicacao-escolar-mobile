package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String)

    @POST("auth/recover")
    suspend fun recoverPassword(@Body request: RecoverPasswordRequest)

    @PATCH("auth/password/reset")
    suspend fun resetPasswordByCode(@Body request: ResetPasswordByCodeRequest)
}
