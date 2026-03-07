package dev.fslab.comunicacao.escolar.network

import android.util.Log
import dev.fslab.comunicacao.escolar.model.RefreshRequest
import dev.fslab.comunicacao.escolar.model.RefreshResponse
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

/**
 * TokenAuthenticator - Renovação automática de tokens em respostas 401
 *
 * Quando o servidor retorna 401 (Unauthorized), tenta renovar o access token
 * usando o refresh token. Se falhar, dispara onSessionExpired.
 */
class TokenAuthenticator : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRIES = 1
        private val gson = Gson()

        private val plainClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "401 recebido em: ${response.request.url.encodedPath}")

        if (responseCount(response) > MAX_RETRIES) {
            Log.w(TAG, "Máximo de tentativas de refresh atingido")
            TokenManager.onSessionExpired?.invoke()
            return null
        }

        val currentRefreshToken = TokenManager.getRefreshToken()
        if (currentRefreshToken.isNullOrEmpty()) {
            Log.w(TAG, "Sem refresh token disponível")
            TokenManager.onSessionExpired?.invoke()
            return null
        }

        synchronized(this) {
            // Verifica se outra thread já fez o refresh
            val currentToken = TokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                Log.d(TAG, "Token já atualizado por outra thread")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            return try {
                val refreshBody = gson.toJson(RefreshRequest(token = currentRefreshToken))
                val mediaType = "application/json; charset=utf-8".toMediaType()

                val refreshRequest = Request.Builder()
                    .url(RetrofitClient.BASE_URL + "auth/refresh")
                    .post(refreshBody.toRequestBody(mediaType))
                    .build()

                val refreshResponse = plainClient.newCall(refreshRequest).execute()

                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body?.string()
                    val parsed = gson.fromJson(body, RefreshResponse::class.java)

                    if (parsed?.isSuccess() == true && parsed.data != null) {
                        TokenManager.saveTokens(parsed.data.token, parsed.data.refresh)
                        TokenManager.onTokensRefreshed?.invoke(parsed.data.token)

                        Log.d(TAG, "Refresh bem-sucedido!")
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${parsed.data.token}")
                            .build()
                    } else {
                        Log.w(TAG, "Refresh falhou: resposta inválida")
                        TokenManager.onSessionExpired?.invoke()
                        null
                    }
                } else {
                    Log.w(TAG, "Refresh HTTP ${refreshResponse.code}")
                    TokenManager.onSessionExpired?.invoke()
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro no refresh: ${e.message}")
                TokenManager.onSessionExpired?.invoke()
                null
            }
        }
    }

    // Conta o número de respostas anteriores para evitar loops infinitos
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
