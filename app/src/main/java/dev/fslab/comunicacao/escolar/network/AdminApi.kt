package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.ApiAuditLog
import dev.fslab.comunicacao.escolar.model.ApiClass
import dev.fslab.comunicacao.escolar.model.ApiDailyLogTemplate
import dev.fslab.comunicacao.escolar.model.ApiResponse
import dev.fslab.comunicacao.escolar.model.ApiSchoolUser
import dev.fslab.comunicacao.escolar.model.ApiStudentInput
import dev.fslab.comunicacao.escolar.model.CreateClassRequest
import dev.fslab.comunicacao.escolar.model.CreateTemplateRequest
import dev.fslab.comunicacao.escolar.model.LinkToSchoolRequest
import dev.fslab.comunicacao.escolar.model.MoveStudentClassRequest
import dev.fslab.comunicacao.escolar.model.PaginatedData
import dev.fslab.comunicacao.escolar.model.UpdateClassRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface AdminApi {

    // Turmas (Classes)

    @GET("schools/{schoolId}/class")
    suspend fun listClasses(
        @Path("schoolId") schoolId: String,
        @QueryMap query: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiClass>>

    @POST("schools/{schoolId}/class")
    suspend fun createClass(
        @Path("schoolId") schoolId: String,
        @Body request: CreateClassRequest
    ): ApiResponse<ApiClass>

    @PATCH("schools/{schoolId}/class/{classId}")
    suspend fun updateClass(
        @Path("schoolId") schoolId: String,
        @Path("classId") classId: String,
        @Body request: UpdateClassRequest
    ): ApiResponse<ApiClass>

    // Usuários (Users)

    @GET("schools/{schoolId}/users")
    suspend fun listUsers(
        @Path("schoolId") schoolId: String,
        @QueryMap query: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiSchoolUser>>

    // Daily Log Templates

    @GET("daily-log-templates")
    suspend fun listTemplates(
        @QueryMap query: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiDailyLogTemplate>>

    @POST("daily-log-templates")
    suspend fun createTemplate(
        @Body request: CreateTemplateRequest
    ): ApiResponse<ApiDailyLogTemplate>

    // Audit Logs

    @GET("schools/{schoolId}/audit-logs")
    suspend fun listAuditLogs(
        @Path("schoolId") schoolId: String,
        @QueryMap query: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedData<ApiAuditLog>>

    // Vincular Usuário

    @POST("schools/{schoolId}/members")
    suspend fun linkToSchool(
        @Path("schoolId") schoolId: String,
        @Body request: LinkToSchoolRequest
    ): ApiResponse<ApiSchoolUser>

    // Filhos (alunos vinculados a responsável)

    @POST("schools/{schoolId}/members/{userId}/students")
    suspend fun addStudentToParent(
        @Path("schoolId") schoolId: String,
        @Path("userId") userId: String,
        @Body request: ApiStudentInput
    ): ApiResponse<Any>

    @PATCH("schools/{schoolId}/members/{parentId}/students/{studentId}")
    suspend fun moveStudentToClass(
        @Path("schoolId") schoolId: String,
        @Path("parentId") parentId: String,
        @Path("studentId") studentId: String,
        @Body request: MoveStudentClassRequest
    ): ApiResponse<Any>

    @DELETE("schools/{schoolId}/members/{userId}/students/{studentId}")
    suspend fun removeStudentFromParent(
        @Path("schoolId") schoolId: String,
        @Path("userId") userId: String,
        @Path("studentId") studentId: String
    ): ApiResponse<Any>

    @DELETE("schools/{schoolId}/members/{userId}")
    suspend fun deactivateMembership(
        @Path("schoolId") schoolId: String,
        @Path("userId") userId: String
    ): ApiResponse<Any>

    @POST("schools/{schoolId}/members/{userId}/restore")
    suspend fun activateMembership(
        @Path("schoolId") schoolId: String,
        @Path("userId") userId: String
    ): ApiResponse<Any>
}
