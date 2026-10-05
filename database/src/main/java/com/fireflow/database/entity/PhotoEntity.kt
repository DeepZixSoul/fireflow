package com.fireflow.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = RevisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["revision_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["revision_id"])]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "revision_id") val revisionId: Long,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long = 0
)
