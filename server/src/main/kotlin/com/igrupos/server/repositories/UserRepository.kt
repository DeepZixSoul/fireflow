package com.igrupos.server.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class UserRepository(private val database: Database) {
    private val logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun findByUsername(username: String): ResultRow? {
        return transaction(database) {
            Users.select { Users.username eq username }
                .firstOrNull()
        }
    }

    fun findById(id: Long): ResultRow? {
        return transaction(database) {
            Users.select { Users.id eq id }
                .firstOrNull()
        }
    }

    fun updatePassword(userId: Long, passwordHash: String) {
        val now = System.currentTimeMillis()
        transaction(database) {
            Users.update({ Users.id eq userId }) {
                it[Users.passwordHash] = passwordHash
                it[Users.mustChangePassword] = false
                it[Users.passwordChangedAt] = now
                it[Users.updatedAt] = now
            }
        }
        logger.info("Password updated for user $userId")
    }

    fun upsert(
        username: String,
        passwordHash: String,
        role: String = "technician",
        isActive: Boolean = true,
        mustChangePassword: Boolean = false
    ) {
        transaction(database) {
            val exists = Users.select { Users.username eq username }
                .count() > 0

            val now = System.currentTimeMillis()

            if (exists) {
                Users.update({ Users.username eq username }) {
                    it[Users.passwordHash] = passwordHash
                    it[Users.role] = role
                    it[Users.isActive] = isActive
                    it[Users.mustChangePassword] = mustChangePassword
                    it[Users.updatedAt] = now
                }
            } else {
                Users.insert {
                    it[Users.username] = username
                    it[Users.passwordHash] = passwordHash
                    it[Users.role] = role
                    it[Users.isActive] = isActive
                    it[Users.mustChangePassword] = mustChangePassword
                    it[Users.createdAt] = now
                    it[Users.updatedAt] = now
                }
            }
        }
    }
}

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20).default("technician")
    val isActive = bool("is_active").default(true)
    val mustChangePassword = bool("must_change_password").default(false)
    val passwordChangedAt = long("password_changed_at").default(0)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}
