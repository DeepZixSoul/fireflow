package com.igrupos.server.repositories

import com.igrupos.server.models.Client
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class ClientRepository(private val database: Database) {
    private val logger = LoggerFactory.getLogger(ClientRepository::class.java)

    fun findById(id: Long): Client? {
        return transaction(database) {
            Clients.select { Clients.id eq id }
                .firstOrNull()
                ?.toClient()
        }
    }

    fun findAll(): List<Client> {
        return transaction(database) {
            Clients.selectAll()
                .map { it.toClient() }
        }
    }

    fun findModifiedSince(timestamp: Long): List<Client> {
        return transaction(database) {
            Clients.select { Clients.updatedAt greaterEq timestamp }
                .map { it.toClient() }
        }
    }

    fun upsert(client: Client) {
        transaction(database) {
            val exists = Clients.select { Clients.id eq client.id }
                .count() > 0

            if (exists) {
                Clients.update({ Clients.id eq client.id }) {
                    it[name] = client.name
                    it[cif] = client.cif
                    it[address] = client.address
                    it[province] = client.province
                    it[contactPerson] = client.contactPerson
                    it[phone] = client.phone
                    it[email] = client.email
                    it[latitude] = client.latitude
                    it[longitude] = client.longitude
                    it[notes] = client.notes
                    it[isActive] = client.isActive
                    it[updatedAt] = client.updatedAt
                }
            } else {
                Clients.insert {
                    it[id] = client.id
                    it[name] = client.name
                    it[cif] = client.cif
                    it[address] = client.address
                    it[province] = client.province
                    it[contactPerson] = client.contactPerson
                    it[phone] = client.phone
                    it[email] = client.email
                    it[latitude] = client.latitude
                    it[longitude] = client.longitude
                    it[notes] = client.notes
                    it[isActive] = client.isActive
                    it[createdAt] = client.createdAt
                    it[updatedAt] = client.updatedAt
                }
            }
        }
    }

    fun deleteById(id: Long) {
        transaction(database) {
            Clients.deleteWhere { Clients.id eq id }
        }
    }
}

object Clients : Table("clients") {
    val id = long("id")
    val name = varchar("name", 255)
    val cif = varchar("cif", 20).nullable()
    val address = text("address").nullable()
    val province = varchar("province", 100).nullable()
    val contactPerson = varchar("contact_person", 255).nullable()
    val phone = varchar("phone", 30).nullable()
    val email = varchar("email", 255).nullable()
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val notes = text("notes").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}

private fun ResultRow.toClient(): Client = Client(
    id = this[Clients.id],
    name = this[Clients.name],
    cif = this[Clients.cif] ?: "",
    address = this[Clients.address] ?: "",
    province = this[Clients.province] ?: "",
    contactPerson = this[Clients.contactPerson] ?: "",
    phone = this[Clients.phone] ?: "",
    email = this[Clients.email] ?: "",
    latitude = this[Clients.latitude],
    longitude = this[Clients.longitude],
    notes = this[Clients.notes] ?: "",
    createdAt = this[Clients.createdAt],
    updatedAt = this[Clients.updatedAt],
    isActive = this[Clients.isActive]
)
