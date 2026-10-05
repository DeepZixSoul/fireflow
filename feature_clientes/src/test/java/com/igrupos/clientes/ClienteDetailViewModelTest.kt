package com.igrupos.clientes

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.igrupos.domain.model.Client
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.model.Motor
import com.igrupos.domain.model.MotorType
import com.igrupos.domain.model.PressureGroup
import com.igrupos.domain.model.PressureMeasurement
import com.igrupos.domain.model.Revision
import com.igrupos.domain.repository.ClientRepository
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
class ClienteDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var clientRepository: ClientRepository
    private lateinit var groupRepository: PressureGroupRepository
    private lateinit var revisionRepository: RevisionRepository
    private lateinit var motorRepository: MotorRepository
    private lateinit var measurementRepository: PressureMeasurementRepository
    private lateinit var context: Context

    private val testClient = Client(
        id = 1L, name = "Acme Corp", cif = "B12345678",
        address = "Calle Mayor 1", province = "Madrid",
        contactPerson = "Juan", phone = "600000000", email = "a@b.com",
        latitude = 40.0, longitude = -3.7, notes = "",
        createdAt = 0L, updatedAt = 0L, isActive = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clientRepository = mockk(relaxed = true)
        groupRepository = mockk(relaxed = true)
        revisionRepository = mockk(relaxed = true)
        motorRepository = mockk(relaxed = true)
        measurementRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        coEvery { clientRepository.getClientById(1L) } returns Result.success(testClient)
        every { groupRepository.getGroupsByClientFlow(1L) } returns flowOf(emptyList())
        coEvery { revisionRepository.getAllRevisions() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clientId: Long = 1L): ClienteDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("clientId" to clientId.toString()))
        return ClienteDetailViewModel(
            savedStateHandle, context, clientRepository,
            groupRepository, revisionRepository, motorRepository, measurementRepository
        )
    }

    @Test
    fun `loadClient sets client state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("Acme Corp", vm.client.value?.name)
        assertEquals("B12345678", vm.client.value?.cif)
    }

    @Test
    fun `loadClient sets null on failure`() = runTest {
        coEvery { clientRepository.getClientById(2L) } returns Result.failure(Exception("Not found"))

        val vm = createViewModel(clientId = 2L)
        advanceUntilIdle()

        assertNull(vm.client.value)
    }

    @Test
    fun `groups are loaded for client`() = runTest {
        val group = PressureGroup(
            id = 10L, clientId = 1L, brand = "Grundfos", model = "CR",
            serialNumber = "SN1", pumpNumber = "P1", manufacturer = "Grundfos",
            power = "5kW", installationDate = null, maintenanceDate = null,
            createdAt = 0L, updatedAt = 0L, isActive = true
        )
        every { groupRepository.getGroupsByClientFlow(1L) } returns flowOf(listOf(group))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.groups.value.size)
        assertEquals("Grundfos", vm.groups.value[0].brand)
    }

    @Test
    fun `group statuses computed as YELLOW when no revisions`() = runTest {
        val group = PressureGroup(
            id = 10L, clientId = 1L, brand = "B", model = "M",
            serialNumber = "S", pumpNumber = "P", manufacturer = "M",
            power = "5kW", installationDate = null, maintenanceDate = null,
            createdAt = 0L, updatedAt = 0L, isActive = true
        )
        every { groupRepository.getGroupsByClientFlow(1L) } returns flowOf(listOf(group))
        coEvery { revisionRepository.getAllRevisions() } returns emptyList()

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(EntityStatus.YELLOW, vm.groupStatuses.value[10L])
    }

    @Test
    fun `deleteGroup calls repository and callback`() = runTest {
        coEvery { groupRepository.deleteGroup(10L) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        var callbackCalled = false
        vm.deleteClient { callbackCalled = true }
        advanceUntilIdle()

        coVerify { clientRepository.deleteClient(1L) }
        assertTrue(callbackCalled)
    }

    @Test
    fun `invalid clientId returns null client`() = runTest {
        coEvery { clientRepository.getClientById(-1L) } returns Result.failure(Exception("Invalid"))

        val savedStateHandle = SavedStateHandle(mapOf("clientId" to "invalid"))
        val vm = ClienteDetailViewModel(
            savedStateHandle, context, clientRepository,
            groupRepository, revisionRepository, motorRepository, measurementRepository
        )
        advanceUntilIdle()

        assertNull(vm.client.value)
    }
}
