package dev.fslab.comunicacao.escolar.network

import com.google.gson.GsonBuilder
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudent
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudentDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "http://localhost:3010/"

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(ApiAssociatedStudent::class.java, ApiAssociatedStudentDeserializer())
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    val userApi: UserApi = retrofit.create(UserApi::class.java)

    val adminApi: AdminApi = retrofit.create(AdminApi::class.java)

    val muralApi: MuralApi = retrofit.create(MuralApi::class.java)
}
