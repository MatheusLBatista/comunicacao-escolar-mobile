package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.ApiResponse
import dev.fslab.comunicacao.escolar.model.CreateDailyLogRequest
import dev.fslab.comunicacao.escolar.model.DailyLogDoc
import dev.fslab.comunicacao.escolar.model.DailyLogsData
import dev.fslab.comunicacao.escolar.model.DailyLogsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface DailyLogsApi {

    @GET("daily-logs")
    suspend fun getDailyLogs(
        @Header("Authorization") token: String
    ): DailyLogsResponse

    @GET("daily-logs")
    suspend fun getDailyLogsFiltered(
        @QueryMap query: Map<String, String>
    ): ApiResponse<DailyLogsData>

    @POST("daily-logs")
    suspend fun createDailyLog(
        @Body request: CreateDailyLogRequest
    ): ApiResponse<DailyLogDoc>

    @PUT("daily-logs/{id}")
    suspend fun updateDailyLog(
        @Path("id") id: String,
        @Body request: CreateDailyLogRequest
    ): ApiResponse<DailyLogDoc>
}
