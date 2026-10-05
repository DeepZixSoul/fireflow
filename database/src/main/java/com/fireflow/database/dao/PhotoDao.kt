package com.fireflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fireflow.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao : BaseDao<PhotoEntity> {
    @Query("SELECT * FROM photos WHERE revision_id = :revisionId ORDER BY timestamp ASC")
    fun getByRevision(revisionId: Long): Flow<List<PhotoEntity>>

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
