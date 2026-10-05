package com.fireflow.revisiones

import androidx.lifecycle.SavedStateHandle
import com.fireflow.domain.model.ChecklistItem
import com.fireflow.domain.model.Revision
import com.fireflow.domain.repository.ChecklistRepository
import com.fireflow.domain.repository.RevisionRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RevisionFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var revisionRepository: RevisionRepository
    private lateinit var checklistRepository: ChecklistRepository

    private val checklistItems = listOf(
        ChecklistItem(1L, "Bomba funcionando", true, 0),
        ChecklistItem(2L, "Presión OK", true, 1),
        ChecklistItem(3L, "Fugas", true, 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        revisionRepository = mockk(relaxed = true)
        checklistRepository = mockk(relaxed = true)

        every { checklistRepository.getChecklistItemsFlow() } returns flowOf(checklistItems)
        coEvery { revisionRepository.getLastRevisionByGroup(10L) } returns Result.success(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(groupId: Long = 10L, revisionId: String? = null): RevisionFormViewModel {
        val map = mutableMapOf<String, Any?>("groupId" to groupId.toString())
        if (revisionId != null) map["revisionId"] = revisionId
        val savedStateHandle = SavedStateHandle(map)
        return RevisionFormViewModel(savedStateHandle, revisionRepository, checklistRepository)
    }

    @Test
    fun `initial state has default values`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.technicianName)
        assertEquals("", vm.uiState.value.notes)
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
        job.cancel()
    }

    @Test
    fun `checklist items are loaded`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        assertEquals(3, vm.checklistItems.value.size)
        assertEquals("Bomba funcionando", vm.checklistItems.value[0].label)
        job.cancel()
    }

    @Test
    fun `onTechnicianNameChange updates state`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        vm.onTechnicianNameChange("Juan Pérez")

        assertEquals("Juan Pérez", vm.uiState.value.technicianName)
        job.cancel()
    }

    @Test
    fun `onChecklistToggle updates checklist results`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        vm.onChecklistToggle("Bomba funcionando", true)

        assertTrue(vm.uiState.value.checklistResults["Bomba funcionando"] == true)
        job.cancel()
    }

    @Test
    fun `save creates revision via repository`() = runTest {
        coEvery { revisionRepository.saveRevision(any()) } returns Result.success(
            Revision(0L, 10L, System.currentTimeMillis(), "Juan", "", emptyMap(), 0L, 0L)
        )

        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        vm.onTechnicianNameChange("Juan Pérez")
        vm.save()
        advanceUntilIdle()

        coVerify { revisionRepository.saveRevision(any()) }
        assertTrue(vm.uiState.value.isSaved)
        job.cancel()
    }

    @Test
    fun `save on failure sets error`() = runTest {
        coEvery { revisionRepository.saveRevision(any()) } returns Result.failure(Exception("DB error"))

        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        vm.onTechnicianNameChange("Juan")
        vm.save()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
        assertNotNull(vm.uiState.value.error)
        job.cancel()
    }

    @Test
    fun `load existing revision populates fields`() = runTest {
        val existing = Revision(
            id = 100L, groupId = 10L, date = 5000L,
            technicianName = "Ana", notes = "Existing notes",
            checklistResults = mapOf("Bomba funcionando" to true, "Presión OK" to false, "Fugas" to true),
            createdAt = 0L, updatedAt = 0L
        )
        coEvery { revisionRepository.getRevisionById(100L) } returns Result.success(existing)

        val vm = createViewModel(revisionId = "100")
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        assertEquals("Ana", vm.uiState.value.technicianName)
        assertEquals("Existing notes", vm.uiState.value.notes)
        assertEquals(5000L, vm.uiState.value.date)
        job.cancel()
    }

    @Test
    fun `toggleCopyFromPrevious changes flag`() = runTest {
        val vm = createViewModel()
        val job = launch { vm.checklistItems.collect {} }
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isCopyFromPrevious)

        vm.toggleCopyFromPrevious()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isCopyFromPrevious)
        job.cancel()
    }
}
