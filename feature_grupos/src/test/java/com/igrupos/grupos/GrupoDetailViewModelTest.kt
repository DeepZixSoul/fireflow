package com.igrupos.grupos

import androidx.lifecycle.SavedStateHandle
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.model.Motor
import com.igrupos.domain.model.MotorType
import com.igrupos.domain.model.PressureGroup
import com.igrupos.domain.model.PressureMeasurement
import com.igrupos.domain.model.Revision
import com.igrupos.domain.repository.MotorRepository
import com.igrupos.domain.repository.PressureGroupRepository
import com.igrupos.domain.repository.PressureMeasurementRepository
import com.igrupos.domain.repository.RevisionRepository
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
class GrupoDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var groupRepository: PressureGroupRepository
    private lateinit var revisionRepository: RevisionRepository
    private lateinit var motorRepository: MotorRepository
    private lateinit var measurementRepository: PressureMeasurementRepository

    private val testGroup = PressureGroup(
        id = 10L, clientId = 1L, brand = "Grundfos", model = "CR 10-10",
        serialNumber = "SN-001", pumpNumber = "P-001", manufacturer = "Grundfos",
        power = "5.5kW", installationDate = 1000L, maintenanceDate = 2000L,
        createdAt = 0L, updatedAt = 0L, isActive = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        groupRepository = mockk(relaxed = true)
        revisionRepository = mockk(relaxed = true)
        motorRepository = mockk(relaxed = true)
        measurementRepository = mockk(relaxed = true)

        coEvery { groupRepository.getGroupById(10L) } returns Result.success(testGroup)
        every { revisionRepository.getRevisionsByGroupFlow(10L) } returns flowOf(emptyList())
        every { motorRepository.getMotorsByGroup(10L) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(groupId: Long = 10L): GrupoDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId.toString()))
        return GrupoDetailViewModel(
            savedStateHandle, groupRepository, revisionRepository,
            motorRepository, measurementRepository
        )
    }

    @Test
    fun `loadGroup sets group state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Grundfos", vm.group.value?.brand)
        assertEquals("CR 10-10", vm.group.value?.model)
    }

    @Test
    fun `loadGroup sets null on failure`() = runTest {
        coEvery { groupRepository.getGroupById(99L) } returns Result.failure(Exception("Not found"))

        val vm = createViewModel(groupId = 99L)
        advanceUntilIdle()

        assertNull(vm.group.value)
    }

    @Test
    fun `revisions are loaded from repository`() = runTest {
        val revision = Revision(
            id = 100L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "", checklistResults = emptyMap(),
            createdAt = 0L, updatedAt = 0L
        )
        every { revisionRepository.getRevisionsByGroupFlow(10L) } returns flowOf(listOf(revision))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.revisions.value.size)
        assertEquals(100L, vm.revisions.value[0].id)
    }

    @Test
    fun `motors are loaded from repository`() = runTest {
        val motor = Motor(
            id = 20L, groupId = 10L, motorType = MotorType.ELECTRIC,
            nominalFlow = 10.0, manometricHeight = 20.0,
            createdAt = 0L, updatedAt = 0L
        )
        every { motorRepository.getMotorsByGroup(10L) } returns flowOf(listOf(motor))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.motors.value.size)
        assertEquals(MotorType.ELECTRIC, vm.motors.value[0].motorType)
    }

    @Test
    fun `revision statuses computed as RED without checklist`() = runTest {
        val revision = Revision(
            id = 100L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "", checklistResults = emptyMap(),
            createdAt = 0L, updatedAt = 0L
        )
        every { revisionRepository.getRevisionsByGroupFlow(10L) } returns flowOf(listOf(revision))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(EntityStatus.RED, vm.revisionStatuses.value[100L])
    }

    @Test
    fun `deleteGroup calls repository and callback`() = runTest {
        coEvery { groupRepository.deleteGroup(10L) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        var callbackCalled = false
        vm.deleteGroup { callbackCalled = true }
        advanceUntilIdle()

        coVerify { groupRepository.deleteGroup(10L) }
        assertTrue(callbackCalled)
    }

    @Test
    fun `onPressureChange updates local state`() = runTest {
        val motor = Motor(
            id = 20L, groupId = 10L, motorType = MotorType.ELECTRIC,
            nominalFlow = 10.0, manometricHeight = 20.0,
            createdAt = 0L, updatedAt = 0L
        )
        every { motorRepository.getMotorsByGroup(10L) } returns flowOf(listOf(motor))
        coEvery { measurementRepository.save(any()) } returns Result.success(
            PressureMeasurement(id = 0L, motorId = 20L, year = 2026, createdAt = 0L, updatedAt = 0L)
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onPressureChange(20L, "0", "1.5")
        advanceUntilIdle()

        val measurement = vm.getMeasurementForMotor(20L)
        assertEquals(1.5, measurement?.pressureAt0 ?: 0.0, 0.001)
    }

    @Test
    fun `invalid groupId returns null group`() = runTest {
        coEvery { groupRepository.getGroupById(-1L) } returns Result.failure(Exception("Invalid"))

        val savedStateHandle = SavedStateHandle(mapOf("groupId" to "invalid"))
        val vm = GrupoDetailViewModel(
            savedStateHandle, groupRepository, revisionRepository,
            motorRepository, measurementRepository
        )
        advanceUntilIdle()

        assertNull(vm.group.value)
    }
}
