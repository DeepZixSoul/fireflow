package com.fireflow.data.repository

import com.fireflow.database.dao.ClientDao
import com.fireflow.database.entity.ClientEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClientRepositoryImplTest {

    private lateinit var clientDao: ClientDao
    private lateinit var repository: ClientRepositoryImpl

    private val testEntity = ClientEntity(
        id = 1L,
        name = "Empresa Test",
        cif = "B12345678",
        address = "Calle Test 1",
        province = "Madrid",
        contactPerson = "Juan",
        phone = "600000000",
        email = "test@test.com",
        isActive = true
    )

    @Before
    fun setup() {
        clientDao = mockk(relaxed = true)
        repository = ClientRepositoryImpl(clientDao)
    }

    @Test
    fun `getClientById returns client when found`() = runTest {
        coEvery { clientDao.getById(1L) } returns testEntity

        val result = repository.getClientById(1L)

        assertTrue(result.isSuccess)
        assertEquals("Empresa Test", result.getOrNull()?.name)
    }

    @Test
    fun `getClientById fails when not found`() = runTest {
        coEvery { clientDao.getById(999L) } returns null

        val result = repository.getClientById(999L)

        assertTrue(result.isFailure)
        assertEquals("Cliente no encontrado", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveClient inserts and returns client with id`() = runTest {
        coEvery { clientDao.insert(any()) } returns 42L

        val client = com.fireflow.domain.model.Client(
            id = 0L,
            name = "New Client",
            cif = "B87654321",
            address = "",
            province = "",
            contactPerson = "",
            phone = "",
            email = "",
            latitude = null,
            longitude = null,
            notes = "",
            createdAt = 0L,
            updatedAt = 0L,
            isActive = true
        )

        val result = repository.saveClient(client)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull()?.id)
        coVerify { clientDao.insert(any()) }
    }

    @Test
    fun `deleteClient soft-deletes by id`() = runTest {
        coEvery { clientDao.softDelete(any(), any()) } returns Unit

        val result = repository.deleteClient(1L)

        assertTrue(result.isSuccess)
        coVerify { clientDao.softDelete(1L, any()) }
    }
}
