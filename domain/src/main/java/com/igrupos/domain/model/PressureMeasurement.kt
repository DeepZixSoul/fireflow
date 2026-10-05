package com.igrupos.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PressureMeasurement(
    val id: Long,
    val motorId: Long,
    val year: Int,
    val pressureAt0: Double? = null,
    val pressureAt50: Double? = null,
    val pressureAt100: Double? = null,
    val pressureAt140: Double? = null,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
