package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaByIdResponse
import dev.fslab.comunicacao.escolar.model.AutorizacoesSaidaResponse
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationRequest
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationResponse
import dev.fslab.comunicacao.escolar.model.CreatePickupLogRequest
import dev.fslab.comunicacao.escolar.model.CreatePickupLogResponse
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoRequest
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoUsedRequest
import dev.fslab.comunicacao.escolar.model.PickupLogsResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AutorizacaoSaidaApi {

    @GET("pickup-authorizations")
    suspend fun getAutorizacoes(
        @Header("Authorization") token: String,
        @Query("school_id") schoolId: String? = null,
        @Query("active") active: Boolean? = null,
        @Query("used") used: Boolean? = null,
        @Query("student_id") studentId: String? = null
    ): AutorizacoesSaidaResponse

    @GET("pickup-authorizations/{id}")
    suspend fun getAutorizacaoById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): AutorizacaoSaidaByIdResponse

    @POST("pickup-authorizations")
    suspend fun criarAutorizacao(
        @Header("Authorization") token: String,
        @Body request: CreatePickupAuthorizationRequest
    ): CreatePickupAuthorizationResponse

    @PATCH("pickup-authorizations/{id}")
    suspend fun cancelarAutorizacao(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: PatchAutorizacaoRequest
    ): Response<Unit>

    @GET("pickup-logs")
    suspend fun getPickupLogs(
        @Header("Authorization") token: String,
        @Query("school_id") schoolId: String? = null
    ): PickupLogsResponse

    @POST("pickup-logs")
    suspend fun criarPickupLog(
        @Header("Authorization") token: String,
        @Body request: CreatePickupLogRequest
    ): CreatePickupLogResponse

    @DELETE("pickup-logs/{id}")
    suspend fun deletarPickupLog(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @PATCH("pickup-authorizations/{id}")
    suspend fun patchAutorizacaoUsed(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: PatchAutorizacaoUsedRequest
    ): Response<Unit>

    @Multipart
    @POST("pickup-authorizations/{id}/photo")
    suspend fun uploadFotoAutorizacao(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part photo: MultipartBody.Part
    ): Response<Unit>
}
