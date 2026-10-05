package com.igrupos.revisiones

import androidx.lifecycle.SavedStateHandle
import com.igrupos.domain.model.Revision
import com.igrupos.domain.repository.RevisionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevisionesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var revisionRepository: RevisionRepository

    private val testRevision = Revision(
        id = 100L, groupId = 10L, date = System.currentTimeMillis(),
        technicianName = "Juan", notes = "Test revision",
        checklistResults = mapOf("Item1" to true, "Item2" to false),
        createdAt = 0L, updatedAt = 0L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        revisionRepository = mockk(relaxed = true)
        every { revisionRepository.getRevisionsByGroupFlow(10L) } returns flowOf(listOf(testRevision))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(groupId: Long = 10L): RevisionesViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId.toString()))
        return RevisionesViewModel(savedStateHandle, revisionRepository)
    }

    @Test
    fun `initial state has empty revisions`() = runTest {
        every { revisionRepository.getRevisionsByGroupFlow(10L) } returns flowOf(emptyList())

        val vm = createViewModel()
        val job = launch { vm.revisions.collect {} }
        advanceUntilIdle()

        assertTrue(vm.revisions.value.isEmpty())
        job.cancel()
    }

    @Test
    fun `revisions are loaded from repository`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.revisions.collect {} }
        advanceUntilIdle()

        assertEquals(1, vm.revisions.value.size)
        assertEquals("Juan", vm.revisions.value[0].technicianName)
        job.cancel()
    }

    @Test
    fun `multiple revisions are loaded`() = runTest {
        val revision2 = testRevision.copy(id = 101L, technicianName = "Ana")
        every { revisionRepository.getRevisionsByGroupFlow(10L) } returns flowOf(listOf(testRevision, revision2))

        val vm = createViewModel()
        val job = launch { vm.revisions.collect {} }
        advanceUntilIdle()

        assertEquals(2, vm.revisions.value.size)
        job.cancel()
    }

    @Test
    fun `refresh toggles isRefreshing`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.revisions.collect {} }
        advanceUntilIdle()

        vm.refresh()
        assertTrue(vm.isRefreshing.value)

        advanceUntilIdle()
        assertFalse(vm.isRefreshing.value)
        job.cancel()
    }

    @Test
    fun `different groupId loads different revisions`() = runTest {
        val otherRevision = testRevision.copy(id = 200L, groupId = 20L)
        every { revisionRepository.getRevisionsByGroupFlow(20L) } returns flowOf(listOf(otherRevision))

        val vm = createViewModel(groupId = 20L)
        val job = launch { vm.revisions.collect {} }
        advanceUntilIdle()

        assertEquals(1, vm.revisions.value.size)
        assertEquals(200L, vm.revisions.value[0].id)
        job.cancel()
    }
}
