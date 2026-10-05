package com.igrupos.data.mapper

import com.igrupos.database.entity.PhotoEntity
import com.igrupos.domain.model.Photo

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
