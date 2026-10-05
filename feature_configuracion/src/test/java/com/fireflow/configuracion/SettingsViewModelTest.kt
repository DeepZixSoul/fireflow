package com.fireflow.configuracion

import android.content.Context
import com.fireflow.domain.repository.SyncManagerRepository
import com.fireflow.domain.repository.SyncSchedulerRepository
import com.fireflow.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var syncManager: SyncManagerRepository
    private lateinit var syncScheduler: SyncSchedulerRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var context: Context
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        syncManager = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        coEvery { syncManager.getServerUrl() } returns "https://api.test.com"
        coEvery { syncManager.getAuthToken() } returns "test-token"
        coEvery { syncManager.isSyncEnabled } returns flowOf(true)
        coEvery { syncManager.lastSyncTime } returns flowOf(1000L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(context, authRepository, syncManager, syncScheduler)
    }

    @Test
    fun `initial state loads config from SyncManager`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("https://api.test.com", viewModel.uiState.value.serverUrl)
        assertEquals("test-token", viewModel.uiState.value.authToken)
        assertTrue(viewModel.uiState.value.syncEnabled)
    }

    @Test
    fun `toggleSync disables sync`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleSync()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.syncEnabled)
        coVerify { syncManager.setSyncEnabled(false) }
        verify { syncScheduler.cancel() }
    }

    @Test
    fun `toggleSync enables sync`() = runTest {
        coEvery { syncManager.isSyncEnabled } returns flowOf(false)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.syncEnabled)

        viewModel.toggleSync()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.syncEnabled)
        coVerify { syncManager.setSyncEnabled(true) }
        verify { syncScheduler.schedule() }
    }

    @Test
    fun `updateServerUrl updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateServerUrl("https://new-server.com")

        assertEquals("https://new-server.com", viewModel.uiState.value.serverUrl)
        assertFalse(viewModel.uiState.value.configSaved)
    }

    @Test
    fun `updateAuthToken updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAuthToken("new-token-123")

        assertEquals("new-token-123", viewModel.uiState.value.authToken)
        assertFalse(viewModel.uiState.value.configSaved)
    }

    @Test
    fun `saveServerConfig persists config`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateServerUrl("https://saved.com")
        viewModel.updateAuthToken("saved-token")
        viewModel.saveServerConfig()
        advanceUntilIdle()

        coVerify { syncManager.saveServerConfig("https://saved.com", "saved-token") }
        assertTrue(viewModel.uiState.value.configSaved)
    }

    @Test
    fun `syncNow calls performSync`() = runTest {
        coEvery { syncManager.performSync() } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.syncNow()
        advanceUntilIdle()

        coVerify { syncManager.performSync() }
        assertFalse(viewModel.uiState.value.isSyncing)
        assertNull(viewModel.uiState.value.syncError)
    }

    @Test
    fun `syncNow shows error on failure`() = runTest {
        coEvery { syncManager.performSync() } returns Result.failure(Exception("Connection failed"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.syncNow()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("Connection failed", viewModel.uiState.value.syncError)
    }

    @Test
    fun `clearSyncError removes error`() = runTest {
        coEvery { syncManager.performSync() } returns Result.failure(Exception("Error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.syncNow()
        advanceUntilIdle()

        viewModel.clearSyncError()

        assertNull(viewModel.uiState.value.syncError)
    }
}
