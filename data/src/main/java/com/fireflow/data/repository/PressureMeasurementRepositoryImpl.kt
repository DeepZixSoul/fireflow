package com.fireflow.data.repository

import com.fireflow.data.mapper.toDomain
import com.fireflow.data.mapper.toEntity
import com.fireflow.database.dao.PressureMeasurementDao
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.repository.PressureMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PressureMeasurementRepositoryImpl @Inject constructor(
    private val dao: PressureMeasurementDao
) : PressureMeasurementRepository {

    override fun getAvailableYears(motorId: Long): Flow<List<Int>> {
        return dao.getAvailableYears(motorId)
    }

    override fun getAvailableYearsForMotors(motorIds: List<Long>): Flow<List<Int>> {
        return dao.getAvailableYearsForMotors(motorIds)
    }

    override suspend fun getByMotorAndYear(motorId: Long, year: Int): PressureMeasurement? {
        return dao.getByMotorAndYear(motorId, year)?.toDomain()
    }

    override suspend fun getByMotorsAndYear(motorIds: List<Long>, year: Int): List<PressureMeasurement> {
        return dao.getByMotorsAndYear(motorIds, year).map { it.toDomain() }
    }

    override fun getByMotorAndYearFlow(motorId: Long, year: Int): Flow<PressureMeasurement?> {
        return dao.getByMotorAndYearFlow(motorId, year).map { it?.toDomain() }
    }

    override fun getAllByMotor(motorId: Long): Flow<List<PressureMeasurement>> {
        return dao.getAllByMotor(motorId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun save(measurement: PressureMeasurement): Result<PressureMeasurement> {
        return try {
            val entity = measurement.toEntity()
            val id = dao.insert(entity)
            Result.success(measurement.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivate(motorId: Long, year: Int): Result<Unit> {
        return try {
            dao.deactivate(motorId, year)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
