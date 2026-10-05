package com.fireflow.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Revision(
    val id: Long,
    val groupId: Long,
    val date: Long,
    val technicianName: String,
    val notes: String,
    val checklistResults: Map<String, Boolean>,
    val createdAt: Long,
    val updatedAt: Long,
    val isDirty: Boolean = false
)
