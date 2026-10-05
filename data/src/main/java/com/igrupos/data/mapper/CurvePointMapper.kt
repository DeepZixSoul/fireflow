package com.igrupos.data.mapper

import com.igrupos.database.entity.CurvePointEntity
import com.igrupos.domain.model.CurvePoint

fun CurvePointEntity.toDomain(): CurvePoint = CurvePoint(
    id = id,
    revisionId = revisionId,
    motorId = motorId,
    flow = flow,
    pressure = pressure,
    orderIndex = orderIndex,
    isDirty = isDirty
)

fun CurvePoint.toEntity(): CurvePointEntity = CurvePointEntity(
    id = id,
    revisionId = revisionId,
    motorId = motorId,
    flow = flow,
    pressure = pressure,
    orderIndex = orderIndex,
    isDirty = isDirty
)
