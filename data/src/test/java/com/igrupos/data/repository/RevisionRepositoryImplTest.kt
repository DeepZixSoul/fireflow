package com.igrupos.data.repository

import com.igrupos.database.dao.RevisionDao
import com.igrupos.database.entity.RevisionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RevisionRepositoryImplTest {

    private lateinit var revisionDao: RevisionDao
    private lateinit var repository: RevisionRepositoryImpl

    private val testEntity = RevisionEntity(
        id = 1L,
        groupId = 10L,
        date = System.currentTimeMillis(),
        technicianName = "Tecnico Test",
        notes = "Revision de prueba",
        checklistResults = """{"Fuga":true}""",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        isDirty = false
    )

    @Before
    fun setup() {
        revisionDao = mockk(relaxed = true)
        repository = RevisionRepositoryImpl(revisionDao)
    }

    @Test
    fun `getRevisionById returns revision when found`() = runTest {
        coEvery { revisionDao.getById(1L) } returns testEntity

        val result = repository.getRevisionById(1L)

        assertTrue(result.isSuccess)
        assertEquals("Tecnico Test", result.getOrNull()?.technicianName)
    }

    @Test
    fun `getRevisionById fails when not found`() = runTest {
        coEvery { revisionDao.getById(999L) } returns null

        val result = repository.getRevisionById(999L)

        assertTrue(result.isFailure)
        assertEquals("Revisión no encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveRevision inserts and sets isDirty true`() = runTest {
        coEvery { revisionDao.insert(any()) } returns 42L

        val revision = com.igrupos.domain.model.Revision(
            id = 0L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "",
            checklistResults = emptyMap(), createdAt = 0L, updatedAt = 0L
        )

        val result = repository.saveRevision(revision)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull()?.id)
        assertTrue(result.getOrNull()?.isDirty == true)
    }

    @Test
    fun `getLastRevisionByGroup returns null when no revisions`() = runTest {
        coEvery { revisionDao.getLastByGroup(10L) } returns null

        val result = repository.getLastRevisionByGroup(10L)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `deleteRevision delegates to dao`() = runTest {
        coEvery { revisionDao.deleteById(any()) } returns Unit

        val result = repository.deleteRevision(1L)

        assertTrue(result.isSuccess)
        coVerify { revisionDao.deleteById(1L) }
    }

    @Test
    fun `getDirtyRevisions returns only dirty revisions`() = runTest {
        val dirtyEntity = testEntity.copy(isDirty = true)
        coEvery { revisionDao.getDirty() } returns listOf(dirtyEntity)

        val result = repository.getDirtyRevisions()

        assertEquals(1, result.size)
        assertTrue(result[0].isDirty)
    }
}
