package com.igrupos.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "revisions",
    foreignKeys = [
        ForeignKey(
            entity = PressureGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["group_id"]),
        Index(value = ["date"])
    ]
)
data class RevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "group_id") val groupId: Long,
    val date: Long,
    @ColumnInfo(name = "technician_name") val technicianName: String = "",
    val notes: String = "",
    @ColumnInfo(name = "checklist_results") val checklistResults: String = "{}",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false
)
