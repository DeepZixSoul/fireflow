package com.fireflow.data.mapper

import com.fireflow.database.entity.PhotoEntity
import com.fireflow.domain.model.Photo

fun PhotoEntity.toDomain(): Photo = Photo(
    id = id,
    revisionId = revisionId,
    filePath = filePath,
    thumbnailPath = thumbnailPath,
    timestamp = timestamp,
    sizeBytes = sizeBytes
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    revisionId = revisionId,
    filePath = filePath,
    thumbnailPath = thumbnailPath,
    timestamp = timestamp,
    sizeBytes = sizeBytes
)
