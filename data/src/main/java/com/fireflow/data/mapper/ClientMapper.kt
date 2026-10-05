package com.fireflow.data.mapper

import com.fireflow.database.entity.ClientEntity
import com.fireflow.domain.model.Client

fun ClientEntity.toDomain(): Client = Client(
    id = id,
    name = name,
    cif = cif,
    address = address,
    province = province,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    latitude = latitude,
    longitude = longitude,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive,
    isDirty = isDirty
)

fun Client.toEntity(): ClientEntity = ClientEntity(
    id = id,
    name = name,
    cif = cif,
    address = address,
    province = province,
    contactPerson = contactPerson,
    phone = phone,
    email = email,
    latitude = latitude,
    longitude = longitude,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive,
    isDirty = isDirty
)
