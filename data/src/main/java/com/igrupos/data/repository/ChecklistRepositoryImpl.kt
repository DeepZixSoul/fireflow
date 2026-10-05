package com.igrupos.data.repository

import com.igrupos.database.dao.ChecklistItemDao
import com.igrupos.database.entity.ChecklistItemEntity
import com.igrupos.domain.model.ChecklistItem
import com.igrupos.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepositoryImpl @Inject constructor(
    private val checklistItemDao: ChecklistItemDao
) : ChecklistRepository {

    override fun getChecklistItemsFlow(): Flow<List<ChecklistItem>> {
        return checklistItemDao.getAllEnabled().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllChecklistItemsFlow(): Flow<List<ChecklistItem>> {
        return checklistItemDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChecklistItems(): Result<List<ChecklistItem>> {
        return try {
            val items = checklistItemDao.getAllEnabled().map { entities ->
                entities.map { it.toDomain() }
            }.first()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveChecklistItem(item: ChecklistItem): Result<ChecklistItem> {
        return try {
            val entity = item.toEntity()
            val id = checklistItemDao.insert(entity)
            Result.success(item.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveChecklistItems(items: List<ChecklistItem>): Result<List<ChecklistItem>> {
        return try {
            val entities = items.map { it.toEntity() }
            val ids = checklistItemDao.insertAll(entities)
            Result.success(items.mapIndexed { index, item -> item.copy(id = ids.getOrElse(index) { 0L }) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ChecklistItemEntity.toDomain(): ChecklistItem = ChecklistItem(
        id = id,
        label = label,
        isEnabled = isEnabled,
        orderIndex = orderIndex
    )

    private fun ChecklistItem.toEntity(): ChecklistItemEntity = ChecklistItemEntity(
        id = id,
        label = label,
        isEnabled = isEnabled,
        orderIndex = orderIndex
    )
}
