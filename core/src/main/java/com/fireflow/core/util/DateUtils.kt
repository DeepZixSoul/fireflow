package com.fireflow.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())

    private val isoFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            .withZone(ZoneId.of("UTC"))

    fun formatDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        return dateFormatter.withZone(ZoneId.systemDefault()).format(instant)
    }

    fun formatDateTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        return dateTimeFormatter.withZone(ZoneId.systemDefault()).format(instant)
    }

    fun formatIso(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        return isoFormatter.format(instant)
    }

    fun parseIso(dateString: String): Long? {
        return try {
            val zonedDateTime = java.time.ZonedDateTime.parse(dateString, isoFormatter)
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            try {
                val instant = Instant.parse(dateString)
                instant.toEpochMilli()
            } catch (e2: Exception) {
                null
            }
        }
    }
}
