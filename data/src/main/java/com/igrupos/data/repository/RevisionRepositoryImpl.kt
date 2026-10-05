package com.igrupos.data.repository

import com.igrupos.data.mapper.toDomain
import com.igrupos.data.mapper.toEntity
import com.igrupos.database.dao.RevisionDao
import com.igrupos.domain.model.Revision
import com.igrupos.domain.repository.RevisionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevisionRepositoryImpl @Inject constructor(
    private val revisionDao: RevisionDao
) : RevisionRepository {

    override fun getRevisionsByGroupFlow(groupId: Long): Flow<List<Revision>> {
        return revisionDao.getByGroup(groupId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllRevisionsFlow(): Flow<List<Revision>> {
        return revisionDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllRevisions(): List<Revision> {
        return revisionDao.getAll().first().map { it.toDomain() }
    }

    override suspend fun getRevisionsByGroup(groupId: Long): Result<List<Revision>> {
        return try {
            val revisions = revisionDao.getByGroup(groupId).map { entities ->
                entities.map { it.toDomain() }
            }.first()
            Result.success(revisions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRevisionsByGroupOneShot(groupId: Long): List<Revision> {
        return try {
            revisionDao.getByGroup(groupId).first().map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRevisionById(id: Long): Result<Revision> {
        return try {
            val entity = revisionDao.getById(id)
                ?: return Result.failure(Exception("Revisión no encontrada"))
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastRevisionByGroup(groupId: Long): Result<Revision?> {
        return try {
            val entity = revisionDao.getLastByGroup(groupId)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveRevision(revision: Revision): Result<Revision> {
        return try {
            val entity = revision.copy(isDirty = true).toEntity()
            val id = revisionDao.insert(entity)
            Result.success(revision.copy(id = id, isDirty = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRevision(id: Long): Result<Unit> {
        return try {
            revisionDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDirtyRevisions(): List<Revision> {
        return revisionDao.getDirty().map { it.toDomain() }
    }

    override suspend fun markRevisionsClean(ids: List<Long>) {
        revisionDao.markClean(ids)
    }
}
