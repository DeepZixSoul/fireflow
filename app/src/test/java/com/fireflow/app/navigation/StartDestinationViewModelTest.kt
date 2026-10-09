package com.fireflow.app.navigation

import com.fireflow.domain.repository.AuthRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartDestinationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start destination stays null until resolved`() = runTest {
        val viewModel = StartDestinationViewModel(authRepository)

        assertNull(viewModel.startDestination.value)
        advanceUntilIdle()
    }

    @Test
    fun `first run without users starts at setup`() = runTest {
        coEvery { authRepository.hasUsers() } returns false

        val viewModel = StartDestinationViewModel(authRepository)
        advanceUntilIdle()

        assertEquals(Routes.SETUP, viewModel.startDestination.value)
    }

    @Test
    fun `existing users start at login`() = runTest {
        coEvery { authRepository.hasUsers() } returns true

        val viewModel = StartDestinationViewModel(authRepository)
        advanceUntilIdle()

        assertEquals(Routes.LOGIN, viewModel.startDestination.value)
    }
}
