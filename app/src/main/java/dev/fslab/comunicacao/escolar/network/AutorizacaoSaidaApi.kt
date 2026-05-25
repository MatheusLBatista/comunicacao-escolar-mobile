package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.AutorizacaoSaidaByIdResponse
import dev.fslab.comunicacao.escolar.model.AutorizacoesSaidaResponse
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationRequest
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationResponse
import dev.fslab.comunicacao.escolar.model.CreatePickupLogRequest
import dev.fslab.comunicacao.escolar.model.CreatePickupLogResponse
import dev.fslab.comunicacao.escolar.model.PatchAutorizacaoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AutorizacaoSaidaApi {

    @GET("pickup-authorizations")
    suspend fun getAutorizacoes(
        @Header("Authorization") token: String,
        @Query("active") active: Boolean = true,
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

    @POST("pickup-logs")
    suspend fun criarPickupLog(
        @Header("Authorization") token: String,
        @Body request: CreatePickupLogRequest
    ): CreatePickupLogResponse
}