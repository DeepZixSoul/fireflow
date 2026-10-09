package com.fireflow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.fireflow.domain.repository.SecuritySettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securitySettingsDataStore by preferencesDataStore(name = "security_settings")

@Singleton
class SecuritySettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecuritySettingsRepository {

    override val flagSecureEnabled: Flow<Boolean> =
        context.securitySettingsDataStore.data.map { it[KEY_FLAG_SECURE] ?: true }

    override suspend fun setFlagSecureEnabled(enabled: Boolean) {
        context.securitySettingsDataStore.edit { it[KEY_FLAG_SECURE] = enabled }
    }

    private companion object {
        val KEY_FLAG_SECURE = booleanPreferencesKey("flag_secure_enabled")
    }
}
