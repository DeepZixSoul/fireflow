package com.fireflow.server.models

import kotlinx.serialization.Serializable

@Serializable
data class CurvePoint(
    val id: Long,
    val revisionId: Long,
    val motorId: Long,
    val flow: Double,
    val pressure: Double,
    val orderIndex: Int,
    val isDirty: Boolean = false
)
