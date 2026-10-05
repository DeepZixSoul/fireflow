package com.fireflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fireflow.database.entity.CurvePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurvePointDao : BaseDao<CurvePointEntity> {
    @Query("SELECT * FROM curve_points ORDER BY revision_id, order_index ASC")
    fun getAll(): Flow<List<CurvePointEntity>>

    @Query("SELECT * FROM curve_points WHERE revision_id = :revisionId ORDER BY order_index ASC")
    fun getByRevision(revisionId: Long): Flow<List<CurvePointEntity>>

    @Query("SELECT * FROM curve_points WHERE revision_id = :revisionId AND motor_id = :motorId ORDER BY order_index ASC")
    fun getByRevisionAndMotor(revisionId: Long, motorId: Long): Flow<List<CurvePointEntity>>

    @Query("SELECT * FROM curve_points WHERE motor_id = :motorId ORDER BY revision_id, order_index ASC")
    fun getByMotor(motorId: Long): Flow<List<CurvePointEntity>>

    @Query("SELECT * FROM curve_points WHERE is_dirty = 1")
    suspend fun getDirty(): List<CurvePointEntity>

    @Query("UPDATE curve_points SET is_dirty = 0 WHERE id IN (:ids)")
    suspend fun markClean(ids: List<Long>)

    @Query("DELETE FROM curve_points WHERE revision_id = :revisionId")
    suspend fun deleteByRevision(revisionId: Long)

    @Query("DELETE FROM curve_points WHERE revision_id = :revisionId AND motor_id = :motorId")
    suspend fun deleteByRevisionAndMotor(revisionId: Long, motorId: Long)
}
