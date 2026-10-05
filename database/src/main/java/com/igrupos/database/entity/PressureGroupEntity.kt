package com.igrupos.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pressure_groups",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["client_id"]),
        Index(value = ["serial_number"])
    ]
)
data class PressureGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "client_id") val clientId: Long,
    val brand: String = "",
    val model: String = "",
    @ColumnInfo(name = "serial_number") val serialNumber: String = "",
    @ColumnInfo(name = "pump_number") val pumpNumber: String = "",
    val manufacturer: String = "",
    val power: String = "",
    @ColumnInfo(name = "installation_date") val installationDate: Long? = null,
    @ColumnInfo(name = "maintenance_date") val maintenanceDate: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false
)
