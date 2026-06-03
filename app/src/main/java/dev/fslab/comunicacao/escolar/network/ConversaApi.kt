package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.ApiConversation
import dev.fslab.comunicacao.escolar.model.ApiMessage
import dev.fslab.comunicacao.escolar.model.ApiResponse
import dev.fslab.comunicacao.escolar.model.CreateConversationRequest
import dev.fslab.comunicacao.escolar.model.MarkReadResponse
import dev.fslab.comunicacao.escolar.model.PaginatedData
import dev.fslab.comunicacao.escolar.model.SendMessageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ConversaApi {

    @POST("schools/{schoolId}/conversations")
    suspend fun findOrCreate(
        @Path("schoolId") schoolId: String,
        @Body req: CreateConversationRequest
    ): ApiResponse<ApiConversation>

    @GET("schools/{schoolId}/conversations")
    suspend fun list(
        @Path("schoolId") schoolId: String,
        @QueryMap q: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiConversation>>

    @GET("conversations/{id}")
    suspend fun get(
        @Path("id") id: String
    ): ApiResponse<ApiConversation>

    @GET("conversations/{conversationId}/messages")
    suspend fun listMessages(
        @Path("conversationId") conversationId: String,
        @QueryMap q: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiMessage>>

    @POST("conversations/{conversationId}/messages")
    suspend fun send(
        @Path("conversationId") conversationId: String,
        @Body req: SendMessageRequest
    ): ApiResponse<ApiMessage>

    @PATCH("conversations/{conversationId}/messages/read")
    suspend fun markRead(
        @Path("conversationId") conversationId: String
    ): ApiResponse<MarkReadResponse>
}
