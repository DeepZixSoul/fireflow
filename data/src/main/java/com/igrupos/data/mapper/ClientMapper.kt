package com.igrupos.data.mapper

import com.igrupos.database.entity.ClientEntity
import com.igrupos.domain.model.Client

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
