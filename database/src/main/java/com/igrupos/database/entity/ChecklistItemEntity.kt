package com.igrupos.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "order_index") val orderIndex: Int
)
