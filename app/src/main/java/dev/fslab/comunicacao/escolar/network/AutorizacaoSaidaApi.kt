package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.AutorizacoesSaidaResponse
import dev.fslab.comunicacao.escolar.model.CancelarAutorizacaoResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface AutorizacaoSaidaApi {

    @GET("pickup-authorizations")
    suspend fun getAutorizacoes(
        @Header("Authorization") token: String
    ): AutorizacoesSaidaResponse

    @DELETE("pickup-authorizations/{id}")
    suspend fun cancelarAutorizacao(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): CancelarAutorizacaoResponse
}