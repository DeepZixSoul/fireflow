package com.igrupos.data.mapper

import com.igrupos.database.entity.UserEntity
import com.igrupos.domain.model.User
import com.igrupos.domain.model.UserRole

fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    displayName = displayName,
    email = email,
    role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.TECHNICIAN },
    isActive = isActive,
    mustChangePassword = mustChangePassword
)

fun User.toEntity(passwordHash: String = ""): UserEntity = UserEntity(
    id = id,
    username = username,
    displayName = displayName,
    email = email,
    passwordHash = passwordHash,
    role = role.name,
    isActive = isActive,
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)
