package com.igrupos.server.repositories

import com.igrupos.server.models.CurvePoint
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class CurveRepository(private val database: Database) {
    private val logger = LoggerFactory.getLogger(CurveRepository::class.java)

    fun findById(id: Long): CurvePoint? {
        return transaction(database) {
            CurvePoints.select { CurvePoints.id eq id }
                .firstOrNull()
                ?.toCurvePoint()
        }
    }

    fun findAll(): List<CurvePoint> {
        return transaction(database) {
            CurvePoints.selectAll()
                .map { it.toCurvePoint() }
        }
    }

    fun findModifiedSince(timestamp: Long): List<CurvePoint> {
        return transaction(database) {
            CurvePoints.select { CurvePoints.updatedAt greaterEq timestamp }
                .map { it.toCurvePoint() }
        }
    }

    fun upsert(curvePoint: CurvePoint) {
        transaction(database) {
            val exists = CurvePoints.select { CurvePoints.id eq curvePoint.id }
                .count() > 0

            if (exists) {
                CurvePoints.update({ CurvePoints.id eq curvePoint.id }) {
                    it[revisionId] = curvePoint.revisionId
                    it[motorId] = curvePoint.motorId
                    it[flow] = curvePoint.flow
                    it[pressure] = curvePoint.pressure
                    it[orderIndex] = curvePoint.orderIndex
                    it[updatedAt] = System.currentTimeMillis()
                }
            } else {
                CurvePoints.insert {
                    it[id] = curvePoint.id
                    it[revisionId] = curvePoint.revisionId
                    it[motorId] = curvePoint.motorId
                    it[flow] = curvePoint.flow
                    it[pressure] = curvePoint.pressure
                    it[orderIndex] = curvePoint.orderIndex
                    it[createdAt] = System.currentTimeMillis()
                    it[updatedAt] = System.currentTimeMillis()
                }
            }
        }
    }

    fun deleteById(id: Long) {
        transaction(database) {
            CurvePoints.deleteWhere { CurvePoints.id eq id }
        }
    }
}

object CurvePoints : Table("curve_points") {
    val id = long("id")
    val revisionId = long("revision_id")
    val motorId = long("motor_id")
    val flow = double("flow").nullable()
    val pressure = double("pressure").nullable()
    val orderIndex = integer("order_index").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

private fun ResultRow.toCurvePoint(): CurvePoint = CurvePoint(
    id = this[CurvePoints.id],
    revisionId = this[CurvePoints.revisionId],
    motorId = this[CurvePoints.motorId],
    flow = this[CurvePoints.flow] ?: 0.0,
    pressure = this[CurvePoints.pressure] ?: 0.0,
    orderIndex = this[CurvePoints.orderIndex] ?: 0
)
