package dev.fslab.comunicacao.escolar.network

import dev.fslab.comunicacao.escolar.model.MuralRequest
import dev.fslab.comunicacao.escolar.model.MuralResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
interface MuralApi {

    @GET("schools/{id}/posts")
    suspend fun getPosts(@Path("id") id: String): List<MuralResponse>

}