package com.fireflow.grupos

import androidx.lifecycle.SavedStateHandle
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.MotorType
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MotorFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var motorRepository: MotorRepository
    private lateinit var measurementRepository: PressureMeasurementRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        motorRepository = mockk(relaxed = true)
        measurementRepository = mockk(relaxed = true)

        every { motorRepository.getMotorsByGroup(10L) } returns flowOf(emptyList())
        every { measurementRepository.getAvailableYearsForMotors(emptyList()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(groupId: Long = 10L): MotorFormViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("groupId" to groupId.toString()))
        return MotorFormViewModel(savedStateHandle, motorRepository, measurementRepository)
    }

    @Test
    fun `initial state has empty motors`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.motors.isEmpty())
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `addMotor adds new entry`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.addMotor()

        assertEquals(1, vm.uiState.value.motors.size)
        assertEquals(MotorType.ELECTRIC, vm.uiState.value.motors[0].motorType)
    }

    @Test
    fun `removeMotor removes entry when more than one`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.addMotor()
        vm.addMotor()
        assertEquals(2, vm.uiState.value.motors.size)

        vm.removeMotor(0)
        assertEquals(1, vm.uiState.value.motors.size)
    }

    @Test
    fun `removeMotor does nothing when only one motor`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.addMotor()
        vm.removeMotor(0)

        assertEquals(1, vm.uiState.value.motors.size)
    }

    @Test
    fun `onMotorTypeChange updates motor type`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.addMotor()
        vm.onMotorTypeChange(0, MotorType.DIESEL)

        assertEquals(MotorType.DIESEL, vm.uiState.value.motors[0].motorType)
    }

    @Test
    fun `save creates motors via repository`() = runTest {
        coEvery { motorRepository.saveMotor(any()) } returns Result.success(
            Motor(1L, 10L, MotorType.ELECTRIC, 10.0, 20.0, 0L, 0L)
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.addMotor()
        vm.onMotorNominalFlowChange(0, "10.0")
        vm.onMotorManometricHeightChange(0, "20.0")
        vm.save()
        advanceUntilIdle()

        coVerify { motorRepository.saveMotor(any()) }
        assertTrue(vm.uiState.value.isSaved)
    }
}
