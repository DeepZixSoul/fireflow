package com.fireflow.domain.model


data class Photo(
    val id: Long,
    val revisionId: Long,
    val filePath: String,
    val thumbnailPath: String,
    val timestamp: Long,
    val sizeBytes: Long
)
