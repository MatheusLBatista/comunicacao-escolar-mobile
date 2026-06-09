package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.ApiEvent
import dev.fslab.comunicacao.escolar.model.ApiResponse
import dev.fslab.comunicacao.escolar.model.CreateEventRequest
import dev.fslab.comunicacao.escolar.model.PaginatedData
import dev.fslab.comunicacao.escolar.model.UpdateEventRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface AgendaApi {

    @GET("events")
    suspend fun listEvents(
        @QueryMap query: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiEvent>>

    @POST("events")
    suspend fun createEvent(
        @Body request: CreateEventRequest
    ): ApiResponse<ApiEvent>

    @PATCH("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: UpdateEventRequest
    ): ApiResponse<ApiEvent>

    @DELETE("events/{id}")
    suspend fun deleteEvent(
        @Path("id") id: String
    ): ApiResponse<ApiEvent>
}
