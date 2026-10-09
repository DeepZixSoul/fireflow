package com.fireflow.domain.repository

import kotlinx.coroutines.flow.Flow

interface SecuritySettingsRepository {

    /** When true, FLAG_SECURE is applied so screenshots and screen recording are blocked. */
    val flagSecureEnabled: Flow<Boolean>

    suspend fun setFlagSecureEnabled(enabled: Boolean)
}
