package com.fireflow.server.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
