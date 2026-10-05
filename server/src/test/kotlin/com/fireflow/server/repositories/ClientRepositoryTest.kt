package com.fireflow.server.repositories

import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.models.Client
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientRepositoryTest {

    private lateinit var repository: ClientRepository

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_client_repo;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        repository = ClientRepository(DatabaseManager.getDatabase())
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun createClient(id: Long = 1L, name: String = "Test Client") = Client(
        id = id,
        name = name,
        cif = "B12345678",
        address = "Calle Test 1",
        province = "Madrid",
        contactPerson = "Juan",
        phone = "600000000",
        email = "test@test.com",
        latitude = 40.4168,
        longitude = -3.7038,
        notes = "Test notes",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isActive = true
    )

    @Test
    fun `findById returns client when found`() {
        repository.upsert(createClient(id = 10L))

        val result = repository.findById(10L)

        assertNotNull(result)
        assertEquals("Test Client", result?.name)
        assertEquals("B12345678", result?.cif)
    }

    @Test
    fun `findById returns null when not found`() {
        val result = repository.findById(999L)
        assertNull(result)
    }

    @Test
    fun `findAll returns all clients`() {
        repository.upsert(createClient(id = 20L, name = "Client A"))
        repository.upsert(createClient(id = 21L, name = "Client B"))

        val result = repository.findAll()
        assertTrue(result.size >= 2)
    }

    @Test
    fun `upsert inserts new client`() {
        val client = createClient(id = 30L, name = "New Client")
        repository.upsert(client)

        val result = repository.findById(30L)
        assertNotNull(result)
        assertEquals("New Client", result?.name)
    }

    @Test
    fun `upsert updates existing client`() {
        repository.upsert(createClient(id = 40L, name = "Old Name"))

        repository.upsert(createClient(id = 40L, name = "New Name"))

        val result = repository.findById(40L)
        assertNotNull(result)
        assertEquals("New Name", result?.name)
    }

    @Test
    fun `findModifiedSince returns clients modified after timestamp`() {
        val past = 1000L
        val future = System.currentTimeMillis() + 100000

        repository.upsert(createClient(id = 50L).copy(updatedAt = future))

        val result = repository.findModifiedSince(future - 1)
        assertTrue(result.any { it.id == 50L })
    }

    @Test
    fun `deleteById removes client`() {
        repository.upsert(createClient(id = 60L))
        assertNotNull(repository.findById(60L))

        repository.deleteById(60L)

        assertNull(repository.findById(60L))
    }
}
