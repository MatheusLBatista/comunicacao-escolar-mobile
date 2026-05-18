package dev.fslab.comunicacao.escolar.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor - Injeta o Bearer token em requisições autenticadas
 *
 * Pula a injeção para endpoints públicos (login, register, recover, refresh)
 * e para requisições que já possuem header Authorization.
 */
class AuthInterceptor : Interceptor {

    companion object {
        private val PUBLIC_PATHS = listOf(
            "auth/login",
            "auth/refresh",
            "auth/recover",
            "auth/password/reset",
            "auth/register"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Não injeta token em endpoints públicos
        if (PUBLIC_PATHS.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        // Não sobrescreve Authorization existente
        val requestWithToken = if (request.header("Authorization") != null) {
            request
        } else {
            val token = TokenManager.getAccessToken()
            if (token != null) {
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }
        }

        val response = chain.proceed(requestWithToken)

        if (response.code == 498) {
            TokenManager.onSessionExpired?.invoke()
        }

        return response
    }
}
