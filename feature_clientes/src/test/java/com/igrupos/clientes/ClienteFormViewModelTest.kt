package com.igrupos.clientes

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.igrupos.domain.model.Client
import com.igrupos.domain.repository.ClientRepository
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
class ClienteFormViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var clientRepository: ClientRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clientRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clientId: String? = null): ClienteFormViewModel {
        val map = mutableMapOf<String, Any?>()
        if (clientId != null) map["clientId"] = clientId
        val savedStateHandle = SavedStateHandle(map)
        return ClienteFormViewModel(savedStateHandle, context, clientRepository)
    }

    @Test
    fun `initial state has empty fields`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.name)
        assertEquals("", vm.uiState.value.cif)
        assertEquals("", vm.uiState.value.address)
        assertNull(vm.uiState.value.nameError)
        assertNull(vm.uiState.value.cifError)
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `onNameChange updates name and clears error`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNameChange("Acme Corp")

        assertEquals("Acme Corp", vm.uiState.value.name)
        assertNull(vm.uiState.value.nameError)
    }

    @Test
    fun `onCifChange updates cif and clears error`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onCifChange("B12345678")

        assertEquals("B12345678", vm.uiState.value.cif)
        assertNull(vm.uiState.value.cifError)
    }

    @Test
    fun `save without name sets nameError`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onCifChange("B12345678")
        vm.save()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.nameError)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `save without cif sets cifError`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNameChange("Acme Corp")
        vm.save()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.cifError)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `save with valid data calls repository`() = runTest {
        coEvery { clientRepository.saveClient(any()) } returns Result.success(
            Client(1L, "Acme", "B123", "", "", "", "", "", null, null, "", 0L, 0L, true)
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNameChange("Acme Corp")
        vm.onCifChange("B12345678")
        vm.save()
        advanceUntilIdle()

        coVerify { clientRepository.saveClient(any()) }
        assertTrue(vm.uiState.value.isSaved)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `save on failure sets error`() = runTest {
        coEvery { clientRepository.saveClient(any()) } returns Result.failure(Exception("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNameChange("Acme Corp")
        vm.onCifChange("B12345678")
        vm.save()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `load existing client populates fields`() = runTest {
        val existingClient = Client(
            id = 1L, name = "Existing Corp", cif = "B99999999",
            address = "Calle Test 1", province = "Sevilla",
            contactPerson = "Pedro", phone = "622222222", email = "e@f.com",
            latitude = 37.0, longitude = -6.0, notes = "test notes",
            createdAt = 0L, updatedAt = 0L, isActive = true
        )
        coEvery { clientRepository.getClientById(1L) } returns Result.success(existingClient)

        val vm = createViewModel(clientId = "1")
        advanceUntilIdle()

        assertEquals("Existing Corp", vm.uiState.value.name)
        assertEquals("B99999999", vm.uiState.value.cif)
        assertEquals("Calle Test 1", vm.uiState.value.address)
        assertEquals("Sevilla", vm.uiState.value.province)
        assertEquals("test notes", vm.uiState.value.notes)
    }
}
