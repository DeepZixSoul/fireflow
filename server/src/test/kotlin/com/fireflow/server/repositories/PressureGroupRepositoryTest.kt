package com.fireflow.server.repositories

import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.models.Client
import com.fireflow.server.models.PressureGroup
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PressureGroupRepositoryTest {

    private lateinit var repository: PressureGroupRepository
    private lateinit var clientRepository: ClientRepository

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_group_repo;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        val db = DatabaseManager.getDatabase()
        clientRepository = ClientRepository(db)
        repository = PressureGroupRepository(db)

        clientRepository.upsert(Client(
            id = 1L, name = "Parent Client", cif = "B00000001",
            address = "", province = "", contactPerson = "", phone = "", email = "",
            latitude = null, longitude = null, notes = "",
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), isActive = true
        ))
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun createGroup(id: Long = 1L, clientId: Long = 1L) = PressureGroup(
        id = id,
        clientId = clientId,
        brand = "Grundfos",
        model = "CR 10-10",
        serialNumber = "SN-001",
        pumpNumber = "P-001",
        manufacturer = "Grundfos",
        power = "5.5kW",
        installationDate = System.currentTimeMillis(),
        maintenanceDate = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isActive = true
    )

    @Test
    fun `findById returns group when found`() {
        repository.upsert(createGroup(id = 10L))

        val result = repository.findById(10L)

        assertNotNull(result)
        assertEquals("Grundfos", result?.brand)
        assertEquals("CR 10-10", result?.model)
    }

    @Test
    fun `findById returns null when not found`() {
        assertNull(repository.findById(999L))
    }

    @Test
    fun `upsert inserts new group`() {
        repository.upsert(createGroup(id = 20L))

        val result = repository.findById(20L)
        assertNotNull(result)
        assertEquals("SN-001", result?.serialNumber)
    }

    @Test
    fun `upsert updates existing group`() {
        repository.upsert(createGroup(id = 30L))

        repository.upsert(createGroup(id = 30L).copy(brand = "Updated Brand"))

        val result = repository.findById(30L)
        assertNotNull(result)
        assertEquals("Updated Brand", result?.brand)
    }

    @Test
    fun `findModifiedSince returns groups modified after timestamp`() {
        val future = System.currentTimeMillis() + 100000
        repository.upsert(createGroup(id = 40L).copy(updatedAt = future))

        val result = repository.findModifiedSince(future - 1)
        assertTrue(result.any { it.id == 40L })
    }

    @Test
    fun `deleteById removes group`() {
        repository.upsert(createGroup(id = 50L))
        assertNotNull(repository.findById(50L))

        repository.deleteById(50L)

        assertNull(repository.findById(50L))
    }
}
