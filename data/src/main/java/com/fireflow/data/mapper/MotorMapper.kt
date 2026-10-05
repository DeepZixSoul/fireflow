package com.fireflow.data.mapper

import com.fireflow.database.entity.MotorEntity
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.MotorType

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
