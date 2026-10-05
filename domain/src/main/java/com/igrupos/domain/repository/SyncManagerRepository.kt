package com.igrupos.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncManagerRepository {
    val isSyncEnabled: Flow<Boolean>
    val lastSyncTime: Flow<Long>
    suspend fun getServerUrl(): String
    suspend fun getAuthToken(): String
    suspend fun setSyncEnabled(enabled: Boolean)
    suspend fun saveServerConfig(url: String, token: String)
    suspend fun performSync(): Result<Unit>
}
