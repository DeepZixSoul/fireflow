package com.igrupos.data.mapper

import com.igrupos.database.entity.RevisionEntity
import com.igrupos.domain.model.Revision
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

fun RevisionEntity.toDomain(): Revision {
    val checklist = try {
        val json = Json.parseToJsonElement(checklistResults) as? JsonObject
        json?.entries?.associate { (key, value) ->
            key to (value.jsonPrimitive.boolean ?: false)
        } ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    return Revision(
        id = id,
        groupId = groupId,
        date = date,
        technicianName = technicianName,
        notes = notes,
        checklistResults = checklist,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDirty = isDirty
    )
}

fun Revision.toEntity(): RevisionEntity {
    val checklistJson = buildJsonObject {
        checklistResults.forEach { (key, value) ->
            put(key, value)
        }
    }.toString()

    return RevisionEntity(
        id = id,
        groupId = groupId,
        date = date,
        technicianName = technicianName,
        notes = notes,
        checklistResults = checklistJson,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDirty = isDirty
    )
}
