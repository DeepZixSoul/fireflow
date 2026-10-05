package com.fireflow.data.sync

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
class ServerConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .setUserAuthenticationRequired(false)
        .build()

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "fireflow_server_config",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private object Keys {
        const val SERVER_URL = "server_url"
        const val AUTH_TOKEN = "auth_token"
    }

    val serverUrl: Flow<String> = flow {
        emit(prefs.getString(Keys.SERVER_URL, "") ?: "")
    }

    val authToken: Flow<String> = flow {
        emit(prefs.getString(Keys.AUTH_TOKEN, "") ?: "")
    }

    suspend fun getServerUrl(): String = prefs.getString(Keys.SERVER_URL, "") ?: ""

    suspend fun getAuthToken(): String = prefs.getString(Keys.AUTH_TOKEN, "") ?: ""

    suspend fun saveConfig(url: String, token: String) {
        prefs.edit().apply {
            putString(Keys.SERVER_URL, url)
            putString(Keys.AUTH_TOKEN, token)
            apply()
        }
    }
}
