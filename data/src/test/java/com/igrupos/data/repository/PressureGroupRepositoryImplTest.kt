package com.igrupos.data.repository

import com.igrupos.database.dao.PressureGroupDao
import com.igrupos.database.entity.PressureGroupEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PressureGroupRepositoryImplTest {

    private lateinit var groupDao: PressureGroupDao
    private lateinit var repository: PressureGroupRepositoryImpl

    private val testEntity = PressureGroupEntity(
        id = 1L,
        clientId = 10L,
        brand = "Grundfos",
        model = "CR 10-10",
        serialNumber = "SN-001",
        pumpNumber = "P-001",
        manufacturer = "Grundfos",
        power = "5.5kW",
        installationDate = 1000L,
        maintenanceDate = null,
        isActive = true,
        isDirty = false
    )

    @Before
    fun setup() {
        groupDao = mockk(relaxed = true)
        repository = PressureGroupRepositoryImpl(groupDao)
    }

    @Test
    fun `getGroupById returns group when found`() = runTest {
        coEvery { groupDao.getById(1L) } returns testEntity

        val result = repository.getGroupById(1L)

        assertTrue(result.isSuccess)
        assertEquals("Grundfos", result.getOrNull()?.brand)
    }

    @Test
    fun `getGroupById fails when not found`() = runTest {
        coEvery { groupDao.getById(999L) } returns null

        val result = repository.getGroupById(999L)

        assertTrue(result.isFailure)
        assertEquals("Grupo no encontrado", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveGroup inserts and sets isDirty true`() = runTest {
        coEvery { groupDao.insert(any()) } returns 42L

        val group = com.igrupos.domain.model.PressureGroup(
            id = 0L, clientId = 10L, brand = "B", model = "M",
            serialNumber = "S", pumpNumber = "P", manufacturer = "M",
            power = "5kW", installationDate = null, maintenanceDate = null,
            createdAt = 0L, updatedAt = 0L, isActive = true
        )

        val result = repository.saveGroup(group)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull()?.id)
        assertTrue(result.getOrNull()?.isDirty == true)
        coVerify { groupDao.insert(any()) }
    }

    @Test
    fun `deleteGroup delegates to dao`() = runTest {
        coEvery { groupDao.softDelete(any(), any()) } returns Unit

        val result = repository.deleteGroup(1L)

        assertTrue(result.isSuccess)
        coVerify { groupDao.softDelete(1L, any()) }
    }

    @Test
    fun `getAllGroups returns mapped groups`() = runTest {
        coEvery { groupDao.getAll() } returns flowOf(listOf(testEntity))

        val result = repository.getAllGroups()

        assertEquals(1, result.size)
        assertEquals("Grundfos", result[0].brand)
    }

    @Test
    fun `getDirtyGroups returns only dirty groups`() = runTest {
        val dirtyEntity = testEntity.copy(isDirty = true)
        coEvery { groupDao.getDirty() } returns listOf(dirtyEntity)

        val result = repository.getDirtyGroups()

        assertEquals(1, result.size)
        assertTrue(result[0].isDirty)
    }
}
