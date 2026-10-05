package com.fireflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fireflow.database.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao : BaseDao<ChecklistItemEntity> {
    @Query("SELECT * FROM checklist_items WHERE is_enabled = 1 ORDER BY order_index ASC")
    fun getAllEnabled(): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY order_index ASC")
    fun getAll(): Flow<List<ChecklistItemEntity>>
}
