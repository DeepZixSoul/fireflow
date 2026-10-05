package com.igrupos.domain.repository

import com.igrupos.domain.model.PressureMeasurement
import kotlinx.coroutines.flow.Flow

interface PressureMeasurementRepository {
    fun getAvailableYears(motorId: Long): Flow<List<Int>>
    suspend fun getByMotorAndYear(motorId: Long, year: Int): PressureMeasurement?
    fun getByMotorAndYearFlow(motorId: Long, year: Int): Flow<PressureMeasurement?>
    fun getAllByMotor(motorId: Long): Flow<List<PressureMeasurement>>
    suspend fun save(measurement: PressureMeasurement): Result<PressureMeasurement>
    suspend fun deactivate(motorId: Long, year: Int): Result<Unit>
    fun getAvailableYearsForMotors(motorIds: List<Long>): Flow<List<Int>>
    suspend fun getByMotorsAndYear(motorIds: List<Long>, year: Int): List<PressureMeasurement>
}
