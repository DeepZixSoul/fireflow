package com.igrupos.data.repository

import com.igrupos.data.mapper.toDomain
import com.igrupos.data.mapper.toEntity
import com.igrupos.database.dao.PressureGroupDao
import com.igrupos.domain.model.PressureGroup
import com.igrupos.domain.repository.PressureGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PressureGroupRepositoryImpl @Inject constructor(
    private val groupDao: PressureGroupDao
) : PressureGroupRepository {

    override suspend fun getAllGroups(): List<PressureGroup> {
        return groupDao.getAll().first().map { it.toDomain() }
    }

    override fun getGroupsByClientFlow(clientId: Long): Flow<List<PressureGroup>> {
        return groupDao.getByClient(clientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGroupsByClient(clientId: Long): Result<List<PressureGroup>> {
        return try {
            val groups = groupDao.getByClient(clientId).map { entities ->
                entities.map { it.toDomain() }
            }.first()
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupById(id: Long): Result<PressureGroup> {
        return try {
            val entity = groupDao.getById(id)
                ?: return Result.failure(Exception("Grupo no encontrado"))
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveGroup(group: PressureGroup): Result<PressureGroup> {
        return try {
            val entity = group.copy(isDirty = true).toEntity()
            val id = groupDao.insert(entity)
            Result.success(group.copy(id = id, isDirty = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(id: Long): Result<Unit> {
        return try {
            groupDao.softDelete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDirtyGroups(): List<PressureGroup> {
        return groupDao.getDirty().map { it.toDomain() }
    }

    override suspend fun markGroupsClean(ids: List<Long>) {
        groupDao.markClean(ids)
    }
}
