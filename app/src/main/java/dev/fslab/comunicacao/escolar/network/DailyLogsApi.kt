package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.DailyLogsResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface DailyLogsApi {

    @GET("daily-logs")
    suspend fun getDailyLogs(
        @Header("Authorization") token: String
    ): DailyLogsResponse
}
