package com.fireflow.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .setUserAuthenticationRequired(false)
        .build()

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "fireflow_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private object Keys {
        const val USER_ID = "user_id"
        const val USERNAME = "username"
        const val DISPLAY_NAME = "display_name"
        const val ROLE = "role"
        const val TOKEN = "token"
        const val SESSION_CREATED_AT = "session_created_at"
    }

    private val sessionExpiryMs: Long = 30L * 24 * 60 * 60 * 1000 // 30 days

    val userId: Flow<Long?> = flow {
        val id = prefs.getLong(Keys.USER_ID, -1).takeIf { it > 0 }
        if (id != null && !isSessionExpired()) {
            emit(id)
        } else {
            emit(null)
        }
    }

    suspend fun saveSession(
        userId: Long,
        username: String,
        displayName: String,
        role: String,
        token: String = ""
    ) {
        prefs.edit().apply {
            putLong(Keys.USER_ID, userId)
            putString(Keys.USERNAME, username)
            putString(Keys.DISPLAY_NAME, displayName)
            putString(Keys.ROLE, role)
            putString(Keys.TOKEN, token)
            putLong(Keys.SESSION_CREATED_AT, System.currentTimeMillis())
            apply()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val userId = prefs.getLong(Keys.USER_ID, -1)
        return userId > 0 && !isSessionExpired()
    }

    suspend fun getUserId(): Long? {
        val id = prefs.getLong(Keys.USER_ID, -1)
        return if (id > 0 && !isSessionExpired()) id else null
    }

    suspend fun clearSession() {
        prefs.edit().clear().apply()
    }

    private fun isSessionExpired(): Boolean {
        val createdAt = prefs.getLong(Keys.SESSION_CREATED_AT, 0)
        if (createdAt == 0L) return true
        return System.currentTimeMillis() - createdAt > sessionExpiryMs
    }
}
