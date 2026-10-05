package com.fireflow.domain.model

enum class UserRole {
    ADMIN,
    TECHNICIAN,
    CLIENT
}

data class User(
    val id: Long,
    val username: String,
    val displayName: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val mustChangePassword: Boolean = false
)
