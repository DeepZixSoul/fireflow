package com.fireflow.data.repository

import com.fireflow.data.mapper.toDomain
import com.fireflow.data.mapper.toEntity
import com.fireflow.database.dao.MotorDao
import com.fireflow.domain.model.Motor
import com.fireflow.domain.repository.MotorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotorRepositoryImpl @Inject constructor(
    private val motorDao: MotorDao
) : MotorRepository {

    override fun getMotorsByGroup(groupId: Long): Flow<List<Motor>> {
        return motorDao.getByGroup(groupId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMotorsByGroupOneShot(groupId: Long): List<Motor> {
        return motorDao.getByGroupOneShot(groupId).map { it.toDomain() }
    }

    override suspend fun getMotorById(id: Long): Motor? {
        return motorDao.getById(id)?.toDomain()
    }

    override suspend fun saveMotor(motor: Motor): Result<Motor> {
        return try {
            val entity = motor.toEntity()
            val id = motorDao.insert(entity)
            Result.success(motor.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMotor(motor: Motor): Result<Unit> {
        return try {
            motorDao.update(motor.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMotor(id: Long): Result<Unit> {
        return try {
            motorDao.deleteByGroup(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMotorsByGroup(groupId: Long): Result<Unit> {
        return try {
            motorDao.deleteByGroup(groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
