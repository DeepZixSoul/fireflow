package com.igrupos.data.repository

import com.igrupos.database.dao.CurvePointDao
import com.igrupos.database.entity.CurvePointEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CurveRepositoryImplTest {

    private lateinit var curveDao: CurvePointDao
    private lateinit var repository: CurveRepositoryImpl

    private val testEntity = CurvePointEntity(
        id = 1L,
        revisionId = 10L,
        motorId = 100L,
        flow = 12.5,
        pressure = 4.5,
        orderIndex = 0,
        isDirty = false
    )

    @Before
    fun setup() {
        curveDao = mockk(relaxed = true)
        repository = CurveRepositoryImpl(curveDao)
    }

    @Test
    fun `getCurvePointsByRevisionAndMotor returns points`() = runTest {
        every { curveDao.getByRevisionAndMotor(10L, 100L) } returns flowOf(listOf(testEntity))

        val result = repository.getCurvePointsByRevisionAndMotorFlow(10L, 100L).first()

        assertEquals(1, result.size)
        assertEquals(12.5, result[0].flow, 0.001)
    }

    @Test
    fun `saveCurvePoint inserts and sets isDirty`() = runTest {
        coEvery { curveDao.insert(any()) } returns 42L

        val point = com.igrupos.domain.model.CurvePoint(
            id = 0L, revisionId = 10L, motorId = 100L,
            flow = 12.5, pressure = 4.5, orderIndex = 0
        )

        val result = repository.saveCurvePoint(point)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isDirty == true)
    }

    @Test
    fun `deleteCurvePointsByRevision delegates to dao`() = runTest {
        coEvery { curveDao.deleteByRevision(any()) } returns Unit

        val result = repository.deleteCurvePointsByRevision(10L)

        assertTrue(result.isSuccess)
        coVerify { curveDao.deleteByRevision(10L) }
    }

    @Test
    fun `getDirtyCurvePoints returns only dirty points`() = runTest {
        val dirtyEntity = testEntity.copy(isDirty = true)
        coEvery { curveDao.getDirty() } returns listOf(dirtyEntity)

        val result = repository.getDirtyCurvePoints()

        assertEquals(1, result.size)
        assertTrue(result[0].isDirty)
    }
}
