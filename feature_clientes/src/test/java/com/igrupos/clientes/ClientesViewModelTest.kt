package com.igrupos.clientes

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var clientRepository: ClientRepository
    private lateinit var groupRepository: PressureGroupRepository
    private lateinit var revisionRepository: RevisionRepository
    private lateinit var motorRepository: MotorRepository
    private lateinit var measurementRepository: PressureMeasurementRepository

    private val testClient = Client(
        id = 1L, name = "Acme Corp", cif = "B12345678",
        address = "Calle Mayor 1", province = "Madrid",
        contactPerson = "Juan", phone = "600000000", email = "a@b.com",
        latitude = 40.0, longitude = -3.7, notes = "",
        createdAt = 0L, updatedAt = 0L, isActive = true
    )

    private val testClient2 = Client(
        id = 2L, name = "Beta SL", cif = "B87654321",
        address = "Calle Menor 2", province = "Barcelona",
        contactPerson = "Ana", phone = "611111111", email = "b@c.com",
        latitude = 41.0, longitude = 2.1, notes = "",
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

        every { clientRepository.getClientsFlow() } returns flowOf(listOf(testClient, testClient2))
        every { clientRepository.searchClientsFlow(any()) } returns flowOf(emptyList())
        coEvery { groupRepository.getAllGroups() } returns emptyList()
        coEvery { revisionRepository.getAllRevisions() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ClientesViewModel {
        return ClientesViewModel(
            clientRepository, groupRepository, revisionRepository,
            motorRepository, measurementRepository
        )
    }

    @Test
    fun `initial state has empty search and is loading`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.searchQuery)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `clients are loaded from repository`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(2, vm.clients.value.size)
        assertEquals("Acme Corp", vm.clients.value[0].name)
    }

    @Test
    fun `search query triggers repository search`() = runTest {
        every { clientRepository.searchClientsFlow("Beta") } returns flowOf(listOf(testClient2))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSearchQueryChanged("Beta")
        advanceUntilIdle()

        assertEquals("Beta", vm.uiState.value.searchQuery)
        coVerify { clientRepository.searchClientsFlow("Beta") }
    }

    @Test
    fun `blank search falls back to all clients`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSearchQueryChanged("Beta")
        advanceUntilIdle()

        vm.onSearchQueryChanged("")
        advanceUntilIdle()

        coVerify { clientRepository.getClientsFlow() }
    }

    @Test
    fun `deleteClient delegates to repository`() = runTest {
        coEvery { clientRepository.deleteClient(1L) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.deleteClient(1L)
        advanceUntilIdle()

        coVerify { clientRepository.deleteClient(1L) }
    }

    @Test
    fun `client statuses are computed for empty groups`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val statuses = vm.clientStatuses.value
        assertEquals(EntityStatus.GRAY, statuses[1L])
        assertEquals(EntityStatus.GRAY, statuses[2L])
    }

    @Test
    fun `client status is RED when group has revision without checklist`() = runTest {
        val group = PressureGroup(
            id = 10L, clientId = 1L, brand = "B", model = "M",
            serialNumber = "S", pumpNumber = "P", manufacturer = "M",
            power = "5kW", installationDate = null, maintenanceDate = null,
            createdAt = 0L, updatedAt = 0L, isActive = true
        )
        val revision = Revision(
            id = 100L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "", checklistResults = emptyMap(),
            createdAt = 0L, updatedAt = 0L
        )

        coEvery { groupRepository.getAllGroups() } returns listOf(group)
        coEvery { revisionRepository.getAllRevisions() } returns listOf(revision)

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(EntityStatus.RED, vm.clientStatuses.value[1L])
    }

    @Test
    fun `client status is GREEN when group has full checklist and pressure data`() = runTest {
        val group = PressureGroup(
            id = 10L, clientId = 1L, brand = "B", model = "M",
            serialNumber = "S", pumpNumber = "P", manufacturer = "M",
            power = "5kW", installationDate = null, maintenanceDate = null,
            createdAt = 0L, updatedAt = 0L, isActive = true
        )
        val revision = Revision(
            id = 100L, groupId = 10L, date = System.currentTimeMillis(),
            technicianName = "Tech", notes = "",
            checklistResults = mapOf("Item1" to true, "Item2" to true),
            createdAt = 0L, updatedAt = 0L
        )
        val motor = Motor(
            id = 20L, groupId = 10L, motorType = MotorType.ELECTRIC,
            nominalFlow = 10.0, manometricHeight = 20.0,
            createdAt = 0L, updatedAt = 0L
        )
        val measurement = PressureMeasurement(
            id = 30L, motorId = 20L, year = 2026,
            pressureAt0 = 1.0, pressureAt50 = 2.0,
            pressureAt100 = 3.0, pressureAt140 = 4.0,
            createdAt = 0L, updatedAt = 0L
        )

        coEvery { groupRepository.getAllGroups() } returns listOf(group)
        coEvery { revisionRepository.getAllRevisions() } returns listOf(revision)
        coEvery { motorRepository.getMotorsByGroupOneShot(10L) } returns listOf(motor)
        coEvery { measurementRepository.getByMotorAndYear(20L, any()) } returns measurement

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(EntityStatus.GREEN, vm.clientStatuses.value[1L])
    }

    @Test
    fun `refresh sets isRefreshing then resets`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.refresh()

        assertTrue(vm.uiState.value.isRefreshing)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isRefreshing)
    }
}
