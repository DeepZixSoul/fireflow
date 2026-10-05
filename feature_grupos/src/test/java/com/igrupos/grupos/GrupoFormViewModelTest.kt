package com.igrupos.grupos

import androidx.lifecycle.SavedStateHandle
import com.igrupos.domain.model.PressureGroup
import com.igrupos.domain.repository.PressureGroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class GrupoFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var groupRepository: PressureGroupRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        groupRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clientId: Long = 1L, groupId: String? = null): GrupoFormViewModel {
        val map = mutableMapOf<String, Any?>("clientId" to clientId.toString())
        if (groupId != null) map["groupId"] = groupId
        val savedStateHandle = SavedStateHandle(map)
        return GrupoFormViewModel(savedStateHandle, groupRepository)
    }

    @Test
    fun `initial state has empty fields`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.brand)
        assertEquals("", vm.uiState.value.model)
        assertEquals("", vm.uiState.value.serialNumber)
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `onBrandChange updates brand`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onBrandChange("Grundfos")

        assertEquals("Grundfos", vm.uiState.value.brand)
    }

    @Test
    fun `save creates group via repository`() = runTest {
        coEvery { groupRepository.saveGroup(any()) } returns Result.success(
            PressureGroup(
                id = 1L, clientId = 1L, brand = "B", model = "M",
                serialNumber = "S", pumpNumber = "P", manufacturer = "M",
                power = "5kW", installationDate = null, maintenanceDate = null,
                createdAt = 0L, updatedAt = 0L, isActive = true
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onBrandChange("Grundfos")
        vm.onModelChange("CR 10-10")
        vm.save()
        advanceUntilIdle()

        coVerify { groupRepository.saveGroup(any()) }
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `save on failure sets error`() = runTest {
        coEvery { groupRepository.saveGroup(any()) } returns Result.failure(Exception("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onBrandChange("Grundfos")
        vm.save()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `load existing group populates fields`() = runTest {
        val existing = PressureGroup(
            id = 10L, clientId = 1L, brand = "Wilo", model = "Yonos",
            serialNumber = "SN-100", pumpNumber = "P-100", manufacturer = "Wilo",
            power = "7.5kW", installationDate = 5000L, maintenanceDate = 6000L,
            createdAt = 0L, updatedAt = 0L, isActive = true
        )
        coEvery { groupRepository.getGroupById(10L) } returns Result.success(existing)

        val vm = createViewModel(groupId = "10")
        advanceUntilIdle()

        assertEquals("Wilo", vm.uiState.value.brand)
        assertEquals("Yonos", vm.uiState.value.model)
        assertEquals("SN-100", vm.uiState.value.serialNumber)
    }
}
