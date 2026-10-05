package com.igrupos.data.mapper

import com.igrupos.database.entity.PressureGroupEntity
import com.igrupos.domain.model.PressureGroup

fun PressureGroupEntity.toDomain(): PressureGroup = PressureGroup(
    id = id,
    clientId = clientId,
    brand = brand,
    model = model,
    serialNumber = serialNumber,
    pumpNumber = pumpNumber,
    manufacturer = manufacturer,
    power = power,
    installationDate = installationDate,
    maintenanceDate = maintenanceDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive,
    isDirty = isDirty
)

fun PressureGroup.toEntity(): PressureGroupEntity = PressureGroupEntity(
    id = id,
    clientId = clientId,
    brand = brand,
    model = model,
    serialNumber = serialNumber,
    pumpNumber = pumpNumber,
    manufacturer = manufacturer,
    power = power,
    installationDate = installationDate,
    maintenanceDate = maintenanceDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive,
    isDirty = isDirty
)
