package dev.fslab.comunicacao.escolar.network

import com.google.gson.GsonBuilder
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudent
import dev.fslab.comunicacao.escolar.model.ApiAssociatedStudentDeserializer
import dev.fslab.comunicacao.escolar.model.PickupLogAuthorization
import dev.fslab.comunicacao.escolar.model.PickupLogAuthorizationDeserializer
import dev.fslab.comunicacao.escolar.model.PickupLogVerifiedBy
import dev.fslab.comunicacao.escolar.model.PickupLogVerifiedByDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = "http://192.168.0.130:3000/"

    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .registerTypeAdapter(ApiAssociatedStudent::class.java, ApiAssociatedStudentDeserializer())
        .registerTypeAdapter(PickupLogAuthorization::class.java, PickupLogAuthorizationDeserializer())
        .registerTypeAdapter(PickupLogVerifiedBy::class.java, PickupLogVerifiedByDeserializer())
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

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val dailyLogsApi: DailyLogsApi by lazy {
        retrofit.create(DailyLogsApi::class.java)
    }

    val adminApi: AdminApi by lazy {
        retrofit.create(AdminApi::class.java)
    }

    val agendaApi: AgendaApi by lazy {
        retrofit.create(AgendaApi::class.java)
    }

    val conversaApi: ConversaApi by lazy {
        retrofit.create(ConversaApi::class.java)
    }

    val autorizacaoSaidaApi: AutorizacaoSaidaApi by lazy {
        retrofit.create(AutorizacaoSaidaApi::class.java)
    }

    val muralApi: MuralApi by lazy {
        retrofit.create(MuralApi::class.java)
    }

    val likeApi: LikeApi by lazy {
        retrofit.create(LikeApi::class.java)
    }
}
