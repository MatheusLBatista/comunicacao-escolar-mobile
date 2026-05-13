package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.DailyLogsResponse
import retrofit2.http.GET

interface DailyLogsApi {

    @GET("daily-logs")
    suspend fun getDailyLogs(): DailyLogsResponse
}

