package com.igrupos.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.igrupos.database.entity.PressureGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PressureGroupDao : BaseDao<PressureGroupEntity> {
    @Query("SELECT * FROM pressure_groups WHERE is_active = 1 ORDER BY brand ASC")
    fun getAll(): Flow<List<PressureGroupEntity>>

    @Query("SELECT * FROM pressure_groups WHERE client_id = :clientId AND is_active = 1 ORDER BY brand ASC")
    fun getByClient(clientId: Long): Flow<List<PressureGroupEntity>>

    @Query("SELECT * FROM pressure_groups WHERE id = :id")
    suspend fun getById(id: Long): PressureGroupEntity?

    @Query("SELECT * FROM pressure_groups WHERE is_dirty = 1")
    suspend fun getDirty(): List<PressureGroupEntity>

    @Query("UPDATE pressure_groups SET is_dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<Long>)

    @Query("UPDATE pressure_groups SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())
}
