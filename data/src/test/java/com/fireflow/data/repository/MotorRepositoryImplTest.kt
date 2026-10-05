package com.fireflow.data.repository

import com.fireflow.database.dao.MotorDao
import com.fireflow.database.entity.MotorEntity
import com.fireflow.domain.model.MotorType
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

class MotorRepositoryImplTest {

    private lateinit var motorDao: MotorDao
    private lateinit var repository: MotorRepositoryImpl

    private val testEntity = MotorEntity(
        id = 1L,
        groupId = 10L,
        motorType = MotorType.ELECTRIC.name,
        nominalFlow = 5.0,
        manometricHeight = 30.0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        motorDao = mockk(relaxed = true)
        repository = MotorRepositoryImpl(motorDao)
    }

    @Test
    fun `getMotorById returns motor when found`() = runTest {
        coEvery { motorDao.getById(1L) } returns testEntity

        val result = repository.getMotorById(1L)

        assertEquals(5.0, result?.nominalFlow)
        assertEquals(MotorType.ELECTRIC, result?.motorType)
    }

    @Test
    fun `getMotorById returns null when not found`() = runTest {
        coEvery { motorDao.getById(999L) } returns null

        val result = repository.getMotorById(999L)

        assertEquals(null, result)
    }

    @Test
    fun `saveMotor inserts and returns motor with id`() = runTest {
        coEvery { motorDao.insert(any()) } returns 42L

        val motor = com.fireflow.domain.model.Motor(
            id = 0L, groupId = 10L, motorType = MotorType.ELECTRIC,
            nominalFlow = 5.0, manometricHeight = 30.0,
            createdAt = 0L, updatedAt = 0L
        )

        val result = repository.saveMotor(motor)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull()?.id)
    }

    @Test
    fun `deleteMotorsByGroup delegates to dao`() = runTest {
        coEvery { motorDao.deleteByGroup(any()) } returns Unit

        val result = repository.deleteMotorsByGroup(10L)

        assertTrue(result.isSuccess)
        coVerify { motorDao.deleteByGroup(10L) }
    }
}
