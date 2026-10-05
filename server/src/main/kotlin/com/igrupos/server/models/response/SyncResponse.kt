package com.igrupos.server.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse<T>(
    val data: List<T>,
    val syncTimestamp: Long
)
