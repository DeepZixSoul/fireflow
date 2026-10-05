package com.igrupos.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.igrupos.database.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao : BaseDao<ClientEntity> {
    @Query("SELECT * FROM clients WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR cif LIKE '%' || :query || '%') ORDER BY name ASC")
    fun search(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE is_dirty = 1")
    suspend fun getDirty(): List<ClientEntity>

    @Query("UPDATE clients SET is_dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<Long>)

    @Query("UPDATE clients SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())
}
