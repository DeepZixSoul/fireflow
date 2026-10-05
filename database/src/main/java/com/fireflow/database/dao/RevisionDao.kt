package com.fireflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fireflow.database.entity.RevisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionDao : BaseDao<RevisionEntity> {
    @Query("SELECT * FROM revisions ORDER BY date DESC")
    fun getAll(): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE group_id = :groupId ORDER BY date DESC")
    fun getByGroup(groupId: Long): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE id = :id")
    suspend fun getById(id: Long): RevisionEntity?

    @Query("SELECT * FROM revisions WHERE group_id = :groupId ORDER BY date DESC LIMIT 1")
    suspend fun getLastByGroup(groupId: Long): RevisionEntity?

    @Query("SELECT * FROM revisions WHERE technician_name LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE is_dirty = 1")
    suspend fun getDirty(): List<RevisionEntity>

    @Query("UPDATE revisions SET is_dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<Long>)

    @Query("DELETE FROM revisions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
