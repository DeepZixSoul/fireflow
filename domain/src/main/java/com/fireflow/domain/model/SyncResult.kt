package com.fireflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncResult<T>(
    val data: List<T>,
    val syncTimestamp: Long
)
