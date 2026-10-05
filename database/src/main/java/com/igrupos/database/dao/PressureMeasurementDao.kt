package com.igrupos.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.igrupos.database.entity.PressureMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PressureMeasurementDao : BaseDao<PressureMeasurementEntity> {
    @Query("SELECT DISTINCT year FROM pressure_measurements WHERE motor_id = :motorId AND is_active = 1 ORDER BY year DESC")
    fun getAvailableYears(motorId: Long): Flow<List<Int>>

    @Query("SELECT DISTINCT year FROM pressure_measurements WHERE motor_id IN (:motorIds) AND is_active = 1 ORDER BY year DESC")
    fun getAvailableYearsForMotors(motorIds: List<Long>): Flow<List<Int>>

    @Query("SELECT * FROM pressure_measurements WHERE motor_id = :motorId AND year = :year")
    suspend fun getByMotorAndYear(motorId: Long, year: Int): PressureMeasurementEntity?

    @Query("SELECT * FROM pressure_measurements WHERE motor_id IN (:motorIds) AND year = :year AND is_active = 1")
    suspend fun getByMotorsAndYear(motorIds: List<Long>, year: Int): List<PressureMeasurementEntity>

    @Query("SELECT * FROM pressure_measurements WHERE motor_id = :motorId AND year = :year")
    fun getByMotorAndYearFlow(motorId: Long, year: Int): Flow<PressureMeasurementEntity?>

    @Query("SELECT * FROM pressure_measurements WHERE motor_id = :motorId AND is_active = 1 ORDER BY year DESC")
    fun getAllByMotor(motorId: Long): Flow<List<PressureMeasurementEntity>>

    @Query("UPDATE pressure_measurements SET is_active = 0 WHERE motor_id = :motorId AND year = :year")
    suspend fun deactivate(motorId: Long, year: Int)
}
