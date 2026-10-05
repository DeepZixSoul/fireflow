package com.igrupos.domain.repository

import com.igrupos.domain.model.ChecklistItem
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {
    suspend fun getChecklistItems(): Result<List<ChecklistItem>>
    suspend fun saveChecklistItem(item: ChecklistItem): Result<ChecklistItem>
    suspend fun saveChecklistItems(items: List<ChecklistItem>): Result<List<ChecklistItem>>
    fun getChecklistItemsFlow(): Flow<List<ChecklistItem>>
    fun getAllChecklistItemsFlow(): Flow<List<ChecklistItem>>
}
