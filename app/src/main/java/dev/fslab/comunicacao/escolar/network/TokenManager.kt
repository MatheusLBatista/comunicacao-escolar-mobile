package dev.fslab.comunicacao.escolar.network

import android.util.Log

/**
 * TokenManager - Gerenciamento centralizado de tokens JWT
 *
 * Singleton thread-safe que armazena access token e refresh token em memória.
 * Callbacks permitem que o ViewModel reaja a expiração de sessão e
 * renovação silenciosa de tokens.
 */
object TokenManager {
    private const val TAG = "TokenManager"

    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var refreshToken: String? = null

    /** Callback disparado quando a sessão expira (tokens inválidos) */
    var onSessionExpired: (() -> Unit)? = null

    /** Callback disparado quando tokens são renovados silenciosamente */
    var onTokensRefreshed: ((String) -> Unit)? = null

    @Synchronized
    fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
        Log.d(TAG, "Tokens salvos (access: ${access.take(20)}…)")
    }

    @Synchronized
    fun getAccessToken(): String? = accessToken

    @Synchronized
    fun getRefreshToken(): String? = refreshToken

    @Synchronized
    fun clearTokens() {
        accessToken = null
        refreshToken = null
        Log.d(TAG, "Tokens limpos")
    }

    fun isAuthenticated(): Boolean = !accessToken.isNullOrEmpty()
}
