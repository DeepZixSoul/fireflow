package com.fireflow.revisiones

import androidx.lifecycle.SavedStateHandle
import com.fireflow.domain.model.Revision
import com.fireflow.domain.repository.RevisionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevisionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var revisionRepository: RevisionRepository

    private val testRevision = Revision(
        id = 100L, groupId = 10L, date = System.currentTimeMillis(),
        technicianName = "Juan", notes = "Test revision",
        checklistResults = mapOf("Item1" to true),
        createdAt = 0L, updatedAt = 0L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        revisionRepository = mockk(relaxed = true)
        coEvery { revisionRepository.getRevisionById(100L) } returns Result.success(testRevision)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(revisionId: Long = 100L): RevisionDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("revisionId" to revisionId.toString()))
        return RevisionDetailViewModel(savedStateHandle, revisionRepository)
    }

    @Test
    fun `loadRevision sets revision state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Juan", vm.revision.value?.technicianName)
        assertEquals(100L, vm.revision.value?.id)
    }

    @Test
    fun `loadRevision sets null on failure`() = runTest {
        coEvery { revisionRepository.getRevisionById(999L) } returns Result.failure(Exception("Not found"))

        val vm = createViewModel(revisionId = 999L)
        advanceUntilIdle()

        assertNull(vm.revision.value)
    }

    @Test
    fun `revision has checklist results`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.revision.value?.checklistResults?.size)
        assertTrue(vm.revision.value?.checklistResults?.get("Item1") == true)
    }

    @Test
    fun `deleteRevision calls repository and callback`() = runTest {
        coEvery { revisionRepository.deleteRevision(100L) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        var callbackCalled = false
        vm.deleteRevision { callbackCalled = true }
        advanceUntilIdle()

        coVerify { revisionRepository.deleteRevision(100L) }
        assertTrue(callbackCalled)
    }

    @Test
    fun `invalid revisionId returns null`() = runTest {
        coEvery { revisionRepository.getRevisionById(-1L) } returns Result.failure(Exception("Invalid"))

        val savedStateHandle = SavedStateHandle(mapOf("revisionId" to "invalid"))
        val vm = RevisionDetailViewModel(savedStateHandle, revisionRepository)
        advanceUntilIdle()

        assertNull(vm.revision.value)
    }
}
