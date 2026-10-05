package com.fireflow.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pressure_measurements",
    foreignKeys = [
        ForeignKey(
            entity = MotorEntity::class,
            parentColumns = ["id"],
            childColumns = ["motor_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["motor_id", "year"], unique = true),
        Index(value = ["motor_id"])
    ]
)
data class PressureMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "motor_id") val motorId: Long,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "pressure_at_0") val pressureAt0: Double? = null,
    @ColumnInfo(name = "pressure_at_50") val pressureAt50: Double? = null,
    @ColumnInfo(name = "pressure_at_100") val pressureAt100: Double? = null,
    @ColumnInfo(name = "pressure_at_140") val pressureAt140: Double? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
