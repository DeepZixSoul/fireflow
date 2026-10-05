package com.fireflow.grupos

import androidx.lifecycle.SavedStateHandle
import com.fireflow.domain.model.EntityStatus
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.MotorType
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.model.Revision
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GruposViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var groupRepository: PressureGroupRepository
    private lateinit var motorRepository: MotorRepository
    private lateinit var revisionRepository: RevisionRepository
    private lateinit var measurementRepository: PressureMeasurementRepository

    private val testGroup = PressureGroup(
        id = 10L, clientId = 1L, brand = "Grundfos", model = "CR 10-10",
        serialNumber = "SN-001", pumpNumber = "P-001", manufacturer = "Grundfos",
        power = "5.5kW", installationDate = null, maintenanceDate = null,
        createdAt = 0L, updatedAt = 0L, isActive = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        groupRepository = mockk(relaxed = true)
        motorRepository = mockk(relaxed = true)
        revisionRepository = mockk(relaxed = true)
        measurementRepository = mockk(relaxed = true)

        every { groupRepository.getGroupsByClientFlow(1L) } returns flowOf(listOf(testGroup))
        every { motorRepository.getMotorsByGroup(10L) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clientId: Long = 1L): GruposViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("clientId" to clientId.toString()))
        return GruposViewModel(
            savedStateHandle, groupRepository, motorRepository,
            revisionRepository, measurementRepository
        )
    }

    @Test
    fun `initial state loads groups for client`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.groupItems.value.size)
        assertEquals("Grundfos", vm.groupItems.value[0].group.brand)
    }

    @Test
    fun `group items include motor types`() = runTest {
        val motor = Motor(
            id = 20L, groupId = 10L, motorType = MotorType.ELECTRIC,
            nominalFlow = 10.0, manometricHeight = 20.0,
            createdAt = 0L, updatedAt = 0L
        )
        every { motorRepository.getMotorsByGroup(10L) } returns flowOf(listOf(motor))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.groupItems.value.size)
        assertEquals(listOf(MotorType.ELECTRIC), vm.groupItems.value[0].motorTypes)
    }

    @Test
    fun `group status is GRAY when no revisions`() = runTest {
        coEvery { revisionRepository.getRevisionsByGroupOneShot(10L) } returns emptyList()

        val vm = createViewModel()
        advanceUntilIdle()

        val items = vm.groupItemsWithStatus.value
        assertEquals(1, items.size)
        assertEquals(EntityStatus.GRAY, items[0].status)
    }

    @Test
    fun `group status is RED when revision has no checklist`() = runTest {
        val revision = Revision(
            id = 100L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "", checklistResults = emptyMap(),
            createdAt = 0L, updatedAt = 0L
        )
        coEvery { revisionRepository.getRevisionsByGroupOneShot(10L) } returns listOf(revision)

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(EntityStatus.RED, vm.groupItemsWithStatus.value[0].status)
    }

    @Test
    fun `group status is GREEN when checklist and pressure data exist`() = runTest {
        val revision = Revision(
            id = 100L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "",
            checklistResults = mapOf("Item1" to true),
            createdAt = 0L, updatedAt = 0L
        )
        val motor = Motor(
            id = 20L, groupId = 10L, motorType = MotorType.ELECTRIC,
            nominalFlow = 10.0, manometricHeight = 20.0,
            createdAt = 0L, updatedAt = 0L
        )
        val measurement = PressureMeasurement(
            id = 30L, motorId = 20L, year = 2026,
            pressureAt0 = 1.0, createdAt = 0L, updatedAt = 0L
        )

        coEvery { revisionRepository.getRevisionsByGroupOneShot(10L) } returns listOf(revision)
        coEvery { motorRepository.getMotorsByGroupOneShot(10L) } returns listOf(motor)
        coEvery { measurementRepository.getByMotorAndYear(20L, any()) } returns measurement

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(EntityStatus.GREEN, vm.groupItemsWithStatus.value[0].status)
    }

    @Test
    fun `empty client returns empty groups`() = runTest {
        every { groupRepository.getGroupsByClientFlow(99L) } returns flowOf(emptyList())

        val vm = createViewModel(clientId = 99L)
        advanceUntilIdle()

        assertTrue(vm.groupItems.value.isEmpty())
        assertTrue(vm.groupItemsWithStatus.value.isEmpty())
    }

    @Test
    fun `refresh toggles isRefreshing`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.refresh()
        assertTrue(vm.isRefreshing.value)

        advanceUntilIdle()
        assertFalse(vm.isRefreshing.value)
    }
}
