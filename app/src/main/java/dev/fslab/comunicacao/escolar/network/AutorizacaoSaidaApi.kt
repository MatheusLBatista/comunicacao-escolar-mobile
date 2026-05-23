package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.AutorizacoesSaidaResponse
import dev.fslab.comunicacao.escolar.model.CancelarAutorizacaoResponse
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationRequest
import dev.fslab.comunicacao.escolar.model.CreatePickupAuthorizationResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AutorizacaoSaidaApi {

    @GET("pickup-authorizations")
    suspend fun getAutorizacoes(
        @Header("Authorization") token: String
    ): AutorizacoesSaidaResponse

    @POST("pickup-authorizations")
    suspend fun criarAutorizacao(
        @Header("Authorization") token: String,
        @Body request: CreatePickupAuthorizationRequest
    ): CreatePickupAuthorizationResponse

    @DELETE("pickup-authorizations/{id}")
    suspend fun cancelarAutorizacao(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): CancelarAutorizacaoResponse
}