package com.igrupos.server.models.request

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest<T>(
    val data: List<T>,
    val lastSyncTimestamp: Long
)
