package com.fireflow.domain.repository

import com.fireflow.domain.model.Motor
import kotlinx.coroutines.flow.Flow

interface MotorRepository {
    fun getMotorsByGroup(groupId: Long): Flow<List<Motor>>
    suspend fun getMotorsByGroupOneShot(groupId: Long): List<Motor>
    suspend fun getMotorById(id: Long): Motor?
    suspend fun saveMotor(motor: Motor): Result<Motor>
    suspend fun updateMotor(motor: Motor): Result<Unit>
    suspend fun deleteMotor(id: Long): Result<Unit>
    suspend fun deleteMotorsByGroup(groupId: Long): Result<Unit>
}
