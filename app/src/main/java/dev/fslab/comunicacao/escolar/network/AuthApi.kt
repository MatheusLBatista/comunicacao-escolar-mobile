package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String)

    @POST("recover")
    suspend fun recoverPassword(@Body request: RecoverPasswordRequest)

    @POST("redefinir-senha")
    suspend fun resetPasswordByCode(@Body request: ResetPasswordByCodeRequest)
}
