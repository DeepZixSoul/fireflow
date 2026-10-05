package com.igrupos.server.repositories

import com.igrupos.server.config.DatabaseConnectionConfig
import com.igrupos.server.config.DatabaseManager
import com.igrupos.server.models.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RevisionRepositoryTest {

    private lateinit var repository: RevisionRepository

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_revision_repo;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        val db = DatabaseManager.getDatabase()
        repository = RevisionRepository(db)

        ClientRepository(db).upsert(Client(
            id = 1L, name = "Client", cif = "B00000001",
            address = "", province = "", contactPerson = "", phone = "", email = "",
            latitude = null, longitude = null, notes = "",
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), isActive = true
        ))
        PressureGroupRepository(db).upsert(PressureGroup(
            id = 1L, clientId = 1L, brand = "B", model = "M", serialNumber = "S",
            pumpNumber = "P", manufacturer = "M", power = "5kW",
            installationDate = null, maintenanceDate = null,
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), isActive = true
        ))
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun createRevision(id: Long = 1L, groupId: Long = 1L) = Revision(
        id = id,
        groupId = groupId,
        date = System.currentTimeMillis(),
        technicianName = "Tecnico Test",
        notes = "Revisión de prueba",
        checklistResults = mapOf("Fuga visible" to true, "Presión OK" to false),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Test
    fun `findById returns revision when found`() {
        repository.upsert(createRevision(id = 10L))

        val result = repository.findById(10L)

        assertNotNull(result)
        assertEquals("Tecnico Test", result?.technicianName)
        assertEquals(2, result?.checklistResults?.size)
    }

    @Test
    fun `findById returns null when not found`() {
        assertNull(repository.findById(999L))
    }

    @Test
    fun `upsert inserts new revision with checklist JSON`() {
        repository.upsert(createRevision(id = 20L))

        val result = repository.findById(20L)
        assertNotNull(result)
        assertEquals(true, result?.checklistResults?.get("Fuga visible"))
        assertEquals(false, result?.checklistResults?.get("Presión OK"))
    }

    @Test
    fun `upsert updates existing revision`() {
        repository.upsert(createRevision(id = 30L))

        repository.upsert(createRevision(id = 30L).copy(technicianName = "Updated"))

        val result = repository.findById(30L)
        assertNotNull(result)
        assertEquals("Updated", result?.technicianName)
    }

    @Test
    fun `findModifiedSince returns revisions modified after timestamp`() {
        val future = System.currentTimeMillis() + 100000
        repository.upsert(createRevision(id = 40L).copy(updatedAt = future))

        val result = repository.findModifiedSince(future - 1)
        assertTrue(result.any { it.id == 40L })
    }

    @Test
    fun `deleteById removes revision`() {
        repository.upsert(createRevision(id = 50L))
        assertNotNull(repository.findById(50L))

        repository.deleteById(50L)

        assertNull(repository.findById(50L))
    }
}
