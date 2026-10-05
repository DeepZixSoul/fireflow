package com.fireflow.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "curve_points",
    foreignKeys = [
        ForeignKey(
            entity = RevisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["revision_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MotorEntity::class,
            parentColumns = ["id"],
            childColumns = ["motor_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["revision_id"]),
        Index(value = ["motor_id"])
    ]
)
data class CurvePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "revision_id") val revisionId: Long,
    @ColumnInfo(name = "motor_id") val motorId: Long,
    val flow: Double,
    val pressure: Double,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false
)
