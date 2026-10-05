package com.igrupos.data.mapper

import com.igrupos.database.entity.MotorEntity
import com.igrupos.domain.model.Motor
import com.igrupos.domain.model.MotorType

fun MotorEntity.toDomain(): Motor = Motor(
    id = id,
    groupId = groupId,
    motorType = try { MotorType.valueOf(motorType) } catch (_: IllegalArgumentException) { MotorType.ELECTRIC },
    nominalFlow = nominalFlow,
    manometricHeight = manometricHeight,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Motor.toEntity(): MotorEntity = MotorEntity(
    id = id,
    groupId = groupId,
    motorType = motorType.name,
    nominalFlow = nominalFlow,
    manometricHeight = manometricHeight,
    createdAt = createdAt,
    updatedAt = updatedAt
)
