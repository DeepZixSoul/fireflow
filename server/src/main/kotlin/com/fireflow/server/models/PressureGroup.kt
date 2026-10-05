package com.fireflow.server.models

import kotlinx.serialization.Serializable

@Serializable
data class PressureGroup(
    val id: Long,
    val clientId: Long,
    val brand: String,
    val model: String,
    val serialNumber: String,
    val pumpNumber: String,
    val manufacturer: String,
    val power: String,
    val installationDate: Long?,
    val maintenanceDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean,
    val isDirty: Boolean = false
)
