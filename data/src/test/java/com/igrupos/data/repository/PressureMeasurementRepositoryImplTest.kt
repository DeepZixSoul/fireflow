package com.igrupos.data.repository

import com.igrupos.database.dao.PressureMeasurementDao
import com.igrupos.database.entity.PressureMeasurementEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PressureMeasurementRepositoryImplTest {

    private lateinit var measurementDao: PressureMeasurementDao
    private lateinit var repository: PressureMeasurementRepositoryImpl

    private val testEntity = PressureMeasurementEntity(
        id = 1L,
        motorId = 10L,
        year = 2025,
        pressureAt0 = 1.0,
        pressureAt50 = 2.5,
        pressureAt100 = 4.0,
        pressureAt140 = 5.5,
        isActive = true
    )

    @Before
    fun setup() {
        measurementDao = mockk(relaxed = true)
        repository = PressureMeasurementRepositoryImpl(measurementDao)
    }

    @Test
    fun `getByMotorAndYear returns measurement when found`() = runTest {
        coEvery { measurementDao.getByMotorAndYear(10L, 2025) } returns testEntity

        val result = repository.getByMotorAndYear(10L, 2025)

        assertEquals(1.0, result?.pressureAt0)
        assertEquals(5.5, result?.pressureAt140)
    }

    @Test
    fun `getByMotorAndYear returns null when not found`() = runTest {
        coEvery { measurementDao.getByMotorAndYear(999L, 2025) } returns null

        val result = repository.getByMotorAndYear(999L, 2025)

        assertNull(result)
    }

    @Test
    fun `save inserts measurement`() = runTest {
        coEvery { measurementDao.insert(any()) } returns 42L

        val measurement = com.igrupos.domain.model.PressureMeasurement(
            id = 0L, motorId = 10L, year = 2025,
            pressureAt0 = 1.0, pressureAt50 = null,
            pressureAt100 = null, pressureAt140 = null,
            isActive = true, createdAt = 0L, updatedAt = 0L
        )

        val result = repository.save(measurement)

        assertTrue(result.isSuccess)
        coVerify { measurementDao.insert(any()) }
    }

    @Test
    fun `deactivate calls dao deactivate`() = runTest {
        coEvery { measurementDao.deactivate(any(), any()) } returns Unit

        val result = repository.deactivate(10L, 2025)

        assertTrue(result.isSuccess)
        coVerify { measurementDao.deactivate(10L, 2025) }
    }

    @Test
    fun `getByMotorsAndYear returns list for multiple motors`() = runTest {
        coEvery { measurementDao.getByMotorsAndYear(listOf(10L, 20L), 2025) } returns listOf(testEntity)

        val result = repository.getByMotorsAndYear(listOf(10L, 20L), 2025)

        assertEquals(1, result.size)
    }
}
