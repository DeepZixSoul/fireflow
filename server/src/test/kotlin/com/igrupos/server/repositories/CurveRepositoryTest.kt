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
class CurveRepositoryTest {

    private lateinit var repository: CurveRepository

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_curve_repo;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        val db = DatabaseManager.getDatabase()
        repository = CurveRepository(db)

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
        RevisionRepository(db).upsert(Revision(
            id = 1L, groupId = 1L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "",
            checklistResults = emptyMap(),
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
        ))
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun createCurvePoint(id: Long = 1L, revisionId: Long = 1L) = CurvePoint(
        id = id,
        revisionId = revisionId,
        motorId = 100L,
        flow = 12.5,
        pressure = 4.5,
        orderIndex = 0
    )

    @Test
    fun `findById returns curve point when found`() {
        repository.upsert(createCurvePoint(id = 10L))

        val result = repository.findById(10L)

        assertNotNull(result)
        assertEquals(12.5, result?.flow)
        assertEquals(4.5, result?.pressure)
    }

    @Test
    fun `findById returns null when not found`() {
        assertNull(repository.findById(999L))
    }

    @Test
    fun `upsert inserts new curve point`() {
        repository.upsert(createCurvePoint(id = 20L))

        val result = repository.findById(20L)
        assertNotNull(result)
        assertEquals(100L, result?.motorId)
        assertEquals(0, result?.orderIndex)
    }

    @Test
    fun `upsert updates existing curve point`() {
        repository.upsert(createCurvePoint(id = 30L))

        repository.upsert(createCurvePoint(id = 30L).copy(flow = 99.9))

        val result = repository.findById(30L)
        assertNotNull(result)
        assertEquals(99.9, result?.flow)
    }

    @Test
    fun `findModifiedSince returns curves modified after timestamp`() {
        val future = System.currentTimeMillis() + 100000
        repository.upsert(createCurvePoint(id = 40L))

        val result = repository.findModifiedSince(0)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `deleteById removes curve point`() {
        repository.upsert(createCurvePoint(id = 50L))
        assertNotNull(repository.findById(50L))

        repository.deleteById(50L)

        assertNull(repository.findById(50L))
    }
}
