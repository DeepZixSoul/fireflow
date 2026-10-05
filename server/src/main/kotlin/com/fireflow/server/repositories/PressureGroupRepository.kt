package com.fireflow.server.repositories

import com.fireflow.server.models.PressureGroup
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class PressureGroupRepository(private val database: Database) {
    private val logger = LoggerFactory.getLogger(PressureGroupRepository::class.java)

    fun findById(id: Long): PressureGroup? {
        return transaction(database) {
            PressureGroups.select { PressureGroups.id eq id }
                .firstOrNull()
                ?.toPressureGroup()
        }
    }

    fun findAll(): List<PressureGroup> {
        return transaction(database) {
            PressureGroups.selectAll()
                .map { it.toPressureGroup() }
        }
    }

    fun findModifiedSince(timestamp: Long): List<PressureGroup> {
        return transaction(database) {
            PressureGroups.select { PressureGroups.updatedAt greaterEq timestamp }
                .map { it.toPressureGroup() }
        }
    }

    fun upsert(group: PressureGroup) {
        transaction(database) {
            val exists = PressureGroups.select { PressureGroups.id eq group.id }
                .count() > 0

            if (exists) {
                PressureGroups.update({ PressureGroups.id eq group.id }) {
                    it[clientId] = group.clientId
                    it[brand] = group.brand
                    it[model] = group.model
                    it[serialNumber] = group.serialNumber
                    it[pumpNumber] = group.pumpNumber
                    it[manufacturer] = group.manufacturer
                    it[power] = group.power
                    it[installationDate] = group.installationDate
                    it[maintenanceDate] = group.maintenanceDate
                    it[isActive] = group.isActive
                    it[updatedAt] = group.updatedAt
                }
            } else {
                PressureGroups.insert {
                    it[id] = group.id
                    it[clientId] = group.clientId
                    it[brand] = group.brand
                    it[model] = group.model
                    it[serialNumber] = group.serialNumber
                    it[pumpNumber] = group.pumpNumber
                    it[manufacturer] = group.manufacturer
                    it[power] = group.power
                    it[installationDate] = group.installationDate
                    it[maintenanceDate] = group.maintenanceDate
                    it[isActive] = group.isActive
                    it[createdAt] = group.createdAt
                    it[updatedAt] = group.updatedAt
                }
            }
        }
    }

    fun deleteById(id: Long) {
        transaction(database) {
            PressureGroups.deleteWhere { PressureGroups.id eq id }
        }
    }
}

object PressureGroups : Table("pressure_groups") {
    val id = long("id")
    val clientId = long("client_id")
    val brand = varchar("brand", 100).nullable()
    val model = varchar("model", 100).nullable()
    val serialNumber = varchar("serial_number", 100).nullable()
    val pumpNumber = varchar("pump_number", 50).nullable()
    val manufacturer = varchar("manufacturer", 100).nullable()
    val power = varchar("power", 50).nullable()
    val installationDate = long("installation_date").nullable()
    val maintenanceDate = long("maintenance_date").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

private fun ResultRow.toPressureGroup(): PressureGroup = PressureGroup(
    id = this[PressureGroups.id],
    clientId = this[PressureGroups.clientId],
    brand = this[PressureGroups.brand] ?: "",
    model = this[PressureGroups.model] ?: "",
    serialNumber = this[PressureGroups.serialNumber] ?: "",
    pumpNumber = this[PressureGroups.pumpNumber] ?: "",
    manufacturer = this[PressureGroups.manufacturer] ?: "",
    power = this[PressureGroups.power] ?: "",
    installationDate = this[PressureGroups.installationDate],
    maintenanceDate = this[PressureGroups.maintenanceDate],
    createdAt = this[PressureGroups.createdAt],
    updatedAt = this[PressureGroups.updatedAt],
    isActive = this[PressureGroups.isActive]
)
