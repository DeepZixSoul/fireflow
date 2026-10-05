package com.fireflow.data.repository

import com.fireflow.data.mapper.toDomain
import com.fireflow.data.mapper.toEntity
import com.fireflow.database.dao.PhotoDao
import com.fireflow.domain.model.Photo
import com.fireflow.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao
) : PhotoRepository {

    override fun getPhotosByRevisionFlow(revisionId: Long): Flow<List<Photo>> {
        return photoDao.getByRevision(revisionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPhotosByRevision(revisionId: Long): Result<List<Photo>> {
        return try {
            val photos = photoDao.getByRevision(revisionId).map { entities ->
                entities.map { it.toDomain() }
            }.first()
            Result.success(photos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePhoto(photo: Photo): Result<Photo> {
        return try {
            val entity = photo.toEntity()
            val id = photoDao.insert(entity)
            Result.success(photo.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhoto(id: Long): Result<Unit> {
        return try {
            photoDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
