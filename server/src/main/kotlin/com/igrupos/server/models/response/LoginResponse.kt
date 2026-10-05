package com.igrupos.server.models.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val expiresIn: Long,
    val role: String,
    val username: String,
    val mustChangePassword: Boolean
)
