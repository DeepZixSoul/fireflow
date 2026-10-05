package com.fireflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fireflow.database.entity.MotorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotorDao : BaseDao<MotorEntity> {
    @Query("SELECT * FROM motors WHERE group_id = :groupId ORDER BY motor_type, id")
    fun getByGroup(groupId: Long): Flow<List<MotorEntity>>

    @Query("SELECT * FROM motors WHERE group_id = :groupId ORDER BY motor_type, id")
    suspend fun getByGroupOneShot(groupId: Long): List<MotorEntity>

    @Query("SELECT * FROM motors WHERE id = :id")
    suspend fun getById(id: Long): MotorEntity?

    @Query("DELETE FROM motors WHERE group_id = :groupId")
    suspend fun deleteByGroup(groupId: Long)
}
