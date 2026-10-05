package com.igrupos.data.mapper

import com.igrupos.database.entity.PressureMeasurementEntity
import com.igrupos.domain.model.PressureMeasurement

fun PressureMeasurementEntity.toDomain(): PressureMeasurement = PressureMeasurement(
    id = id,
    motorId = motorId,
    year = year,
    pressureAt0 = pressureAt0,
    pressureAt50 = pressureAt50,
    pressureAt100 = pressureAt100,
    pressureAt140 = pressureAt140,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PressureMeasurement.toEntity(): PressureMeasurementEntity = PressureMeasurementEntity(
    id = id,
    motorId = motorId,
    year = year,
    pressureAt0 = pressureAt0,
    pressureAt50 = pressureAt50,
    pressureAt100 = pressureAt100,
    pressureAt140 = pressureAt140,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)
