package com.igrupos.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clients",
    indices = [
        Index(value = ["name", "cif"], unique = true),
        Index(value = ["cif"]),
        Index(value = ["is_active"])
    ]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cif: String,
    val address: String = "",
    val province: String = "",
    @ColumnInfo(name = "contact_person") val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false
)
