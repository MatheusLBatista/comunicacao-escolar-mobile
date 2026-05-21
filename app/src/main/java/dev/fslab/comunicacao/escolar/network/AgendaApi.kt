package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.ApiEvent
import dev.fslab.comunicacao.escolar.model.ApiResponse
import dev.fslab.comunicacao.escolar.model.PaginatedData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface AgendaApi {

    @GET("schools/{schoolId}/events")
    suspend fun listEvents(
        @Path("schoolId") schoolId: String,
        @QueryMap query: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiEvent>>
}
