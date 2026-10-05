package com.fireflow.domain.repository

import com.fireflow.domain.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    suspend fun getPhotosByRevision(revisionId: Long): Result<List<Photo>>
    suspend fun savePhoto(photo: Photo): Result<Photo>
    suspend fun deletePhoto(id: Long): Result<Unit>
    fun getPhotosByRevisionFlow(revisionId: Long): Flow<List<Photo>>
}
