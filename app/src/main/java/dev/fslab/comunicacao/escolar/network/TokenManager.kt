package dev.fslab.comunicacao.escolar.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val TAG = "TokenManager"
    private const val PREFS_NAME = "auth_tokens"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_SCHOOL_ID = "school_id"
    private const val KEY_STUDENTS = "students_json"

    @Volatile private var accessToken: String? = null
    @Volatile private var refreshToken: String? = null
    @Volatile private var userEmail: String? = null
    @Volatile private var userId: String? = null
    @Volatile private var userName: String? = null
    @Volatile private var userRole: String? = null
    @Volatile private var schoolId: String? = null
    @Volatile private var studentsJson: String? = null
    private var prefs: SharedPreferences? = null

    var onSessionExpired: (() -> Unit)? = null
    var onTokensRefreshed: ((String) -> Unit)? = null

    @Synchronized
    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        accessToken = prefs?.getString(KEY_ACCESS, null)
        refreshToken = prefs?.getString(KEY_REFRESH, null)
        userEmail = prefs?.getString(KEY_EMAIL, null)
        userId = prefs?.getString(KEY_USER_ID, null)
        userName = prefs?.getString(KEY_USER_NAME, null)
        userRole = prefs?.getString(KEY_USER_ROLE, null)
        schoolId = prefs?.getString(KEY_SCHOOL_ID, null)
        studentsJson = prefs?.getString(KEY_STUDENTS, null)
        Log.d(TAG, "TokenManager init. Autenticado: ${isAuthenticated()}")
    }

    data class UserInfo(
        val id: String,
        val name: String,
        val email: String,
        val role: String,
        val schoolId: String?
    )

    @Synchronized
    fun saveTokens(access: String, refresh: String, user: UserInfo? = null) {
        accessToken = access
        refreshToken = refresh
        prefs?.edit()
            ?.putString(KEY_ACCESS, access)
            ?.putString(KEY_REFRESH, refresh)
            ?.also { editor ->
                if (user != null) {
                    userEmail = user.email
                    userId = user.id
                    userName = user.name
                    userRole = user.role
                    schoolId = user.schoolId
                    editor.putString(KEY_EMAIL, user.email)
                    editor.putString(KEY_USER_ID, user.id)
                    editor.putString(KEY_USER_NAME, user.name)
                    editor.putString(KEY_USER_ROLE, user.role)
                    if (user.schoolId != null) editor.putString(KEY_SCHOOL_ID, user.schoolId)
                    else editor.remove(KEY_SCHOOL_ID)
                }
            }
            ?.apply()
        Log.d(TAG, "Tokens salvos (access: ${access.take(20)}…)")
    }

    @Synchronized
    fun saveStudentsJson(json: String) {
        studentsJson = json
        prefs?.edit()?.putString(KEY_STUDENTS, json)?.apply()
    }

    @Synchronized
    fun getStudentsJson(): String? = studentsJson

    @Synchronized
    fun getAccessToken(): String? = accessToken

    @Synchronized
    fun getRefreshToken(): String? = refreshToken

    @Synchronized
    fun getSavedUser(): UserInfo? {
        val id = userId ?: return null
        val name = userName ?: return null
        val email = userEmail ?: return null
        val role = userRole ?: return null
        return UserInfo(id, name, email, role, schoolId)
    }

    @Synchronized
    fun clearTokens() {
        accessToken = null
        refreshToken = null
        userEmail = null
        userId = null
        userName = null
        userRole = null
        schoolId = null
        studentsJson = null
        prefs?.edit()
            ?.remove(KEY_ACCESS)?.remove(KEY_REFRESH)?.remove(KEY_EMAIL)
            ?.remove(KEY_USER_ID)?.remove(KEY_USER_NAME)?.remove(KEY_USER_ROLE)?.remove(KEY_SCHOOL_ID)
            ?.remove(KEY_STUDENTS)
            ?.apply()
        Log.d(TAG, "Tokens limpos")
    }

    fun isAuthenticated(): Boolean = !accessToken.isNullOrEmpty()
}
