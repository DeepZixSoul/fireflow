package com.igrupos.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Motor(
    val id: Long,
    val groupId: Long,
    val motorType: MotorType,
    val nominalFlow: Double,
    val manometricHeight: Double,
    val createdAt: Long,
    val updatedAt: Long
)
