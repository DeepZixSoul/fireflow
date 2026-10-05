package com.fireflow.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: Long,
    val name: String,
    val cif: String,
    val address: String,
    val province: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val latitude: Double?,
    val longitude: Double?,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean,
    val isDirty: Boolean = false
)
