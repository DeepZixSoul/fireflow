package com.fireflow.server.repositories

import com.fireflow.server.models.Revision
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class RevisionRepository(private val database: Database) {
    private val logger = LoggerFactory.getLogger(RevisionRepository::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    fun findById(id: Long): Revision? {
        return transaction(database) {
            Revisions.select { Revisions.id eq id }
                .firstOrNull()
                ?.toRevision()
        }
    }

    fun findAll(): List<Revision> {
        return transaction(database) {
            Revisions.selectAll()
                .map { it.toRevision() }
        }
    }

    fun findModifiedSince(timestamp: Long): List<Revision> {
        return transaction(database) {
            Revisions.select { Revisions.updatedAt greaterEq timestamp }
                .map { it.toRevision() }
        }
    }

    fun upsert(revision: Revision) {
        transaction(database) {
            val exists = Revisions.select { Revisions.id eq revision.id }
                .count() > 0

            val checklistJson = json.encodeToString(revision.checklistResults)

            if (exists) {
                Revisions.update({ Revisions.id eq revision.id }) {
                    it[groupId] = revision.groupId
                    it[date] = revision.date
                    it[technicianName] = revision.technicianName
                    it[notes] = revision.notes
                    it[checklistResults] = checklistJson
                    it[updatedAt] = revision.updatedAt
                }
            } else {
                Revisions.insert {
                    it[id] = revision.id
                    it[groupId] = revision.groupId
                    it[date] = revision.date
                    it[technicianName] = revision.technicianName
                    it[notes] = revision.notes
                    it[checklistResults] = checklistJson
                    it[createdAt] = revision.createdAt
                    it[updatedAt] = revision.updatedAt
                }
            }
        }
    }

    fun deleteById(id: Long) {
        transaction(database) {
            Revisions.deleteWhere { Revisions.id eq id }
        }
    }
}

object Revisions : Table("revisions") {
    val id = long("id")
    val groupId = long("group_id")
    val date = long("date")
    val technicianName = varchar("technician_name", 255).nullable()
    val notes = text("notes").nullable()
    val checklistResults = text("checklist_results").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

private fun ResultRow.toRevision(): Revision {
    val checklistStr = this[Revisions.checklistResults] ?: "{}"
    val checklist: Map<String, Boolean> = try {
        Json { ignoreUnknownKeys = true }.decodeFromString(checklistStr)
    } catch (e: Exception) {
        emptyMap()
    }

    return Revision(
        id = this[Revisions.id],
        groupId = this[Revisions.groupId],
        date = this[Revisions.date],
        technicianName = this[Revisions.technicianName] ?: "",
        notes = this[Revisions.notes] ?: "",
        checklistResults = checklist,
        createdAt = this[Revisions.createdAt],
        updatedAt = this[Revisions.updatedAt]
    )
}
