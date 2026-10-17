package com.project.whobusy.presentation

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.RegistrationDraft
import com.project.whobusy.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successfulRestorePublishesReadyTogetherWithUserWithoutSignedOutFrame() = runTest {
        val auth = StartupAuthRepository()
        val restoring = CompletableDeferred<Unit>()
        auth.restore = {
            restoring.await()
            auth.users.value = DemoAccount.user
            Result.success(Unit)
        }
        val viewModel = AppViewModel(auth)
        val history = mutableListOf<AppState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { history.add(it) }
        }
        runCurrent()

        assertEquals(AppStartup.Loading, viewModel.state.value.startup)
        viewModel.onEvent(AppCommand.RetryRestore)
        viewModel.onEvent(AppCommand.SignInAgain)
        assertEquals(1, auth.restoreCalls)
        assertEquals(0, auth.logoutCalls)

        restoring.complete(Unit)
        advanceUntilIdle()

        assertEquals(AppState(DemoAccount.user, AppStartup.Ready), viewModel.state.value)
        assertTrue(history.any { it.startup == AppStartup.Ready })
        assertFalse(history.any { it.startup == AppStartup.Ready && it.user == null })
    }

    @Test
    fun noSavedSessionOpensSignedOutNavigationAfterRestore() = runTest {
        val auth = StartupAuthRepository()
        val viewModel = AppViewModel(auth)

        advanceUntilIdle()

        assertEquals(AppStartup.Ready, viewModel.state.value.startup)
        assertNull(viewModel.state.value.user)
        assertEquals(1, auth.restoreCalls)
        assertEquals(0, auth.logoutCalls)
    }

    @Test
    fun failedRestoreKeepsStartupScreenAndRetryRestoresTheSameSession() = runTest {
        val auth = StartupAuthRepository()
        auth.restore = { Result.failure(IOException("Offline")) }
        val viewModel = AppViewModel(auth)
        advanceUntilIdle()

        assertEquals(AppStartup.Failed, viewModel.state.value.startup)
        assertEquals(0, auth.logoutCalls)
        auth.restore = {
            auth.users.value = DemoAccount.user
            Result.success(Unit)
        }
        viewModel.onEvent(AppCommand.RetryRestore)
        assertEquals(AppStartup.Loading, viewModel.state.value.startup)
        advanceUntilIdle()

        assertEquals(AppState(DemoAccount.user, AppStartup.Ready), viewModel.state.value)
        assertEquals(2, auth.restoreCalls)
    }

    @Test
    fun signInAgainWaitsForSuccessfulLogoutBeforeOpeningLogin() = runTest {
        val auth = StartupAuthRepository()
        auth.users.value = DemoAccount.user
        auth.restore = { Result.failure(IOException("Offline")) }
        val loggingOut = CompletableDeferred<Unit>()
        auth.logoutAction = {
            loggingOut.await()
            auth.users.value = null
            Result.success(Unit)
        }
        val viewModel = AppViewModel(auth)
        advanceUntilIdle()

        viewModel.onEvent(AppCommand.SignInAgain)
        runCurrent()
        assertEquals(AppStartup.Loading, viewModel.state.value.startup)
        assertEquals(DemoAccount.user, viewModel.state.value.user)
        viewModel.onEvent(AppCommand.SignInAgain)
        viewModel.onEvent(AppCommand.RetryRestore)
        assertEquals(1, auth.logoutCalls)
        assertEquals(1, auth.restoreCalls)

        loggingOut.complete(Unit)
        advanceUntilIdle()

        assertEquals(AppState(user = null, startup = AppStartup.Ready), viewModel.state.value)
    }

    @Test
    fun failedLogoutKeepsFailureAndAllowsAnotherAttempt() = runTest {
        val auth = StartupAuthRepository()
        auth.users.value = DemoAccount.user
        auth.restore = { Result.failure(IOException("Offline")) }
        auth.logoutAction = { Result.failure(IOException("Storage unavailable")) }
        val viewModel = AppViewModel(auth)
        advanceUntilIdle()

        viewModel.onEvent(AppCommand.SignInAgain)
        advanceUntilIdle()

        assertEquals(AppStartup.Failed, viewModel.state.value.startup)
        assertEquals(DemoAccount.user, viewModel.state.value.user)
        auth.logoutAction = {
            auth.users.value = null
            Result.success(Unit)
        }
        viewModel.onEvent(AppCommand.SignInAgain)
        advanceUntilIdle()

        assertEquals(AppState(user = null, startup = AppStartup.Ready), viewModel.state.value)
        assertEquals(2, auth.logoutCalls)
    }

    @Test
    fun observesLoginAndLogoutAfterStartupWithoutRestoringAgain() = runTest {
        val auth = StartupAuthRepository()
        val viewModel = AppViewModel(auth)
        advanceUntilIdle()

        auth.users.value = DemoAccount.user
        runCurrent()
        assertEquals(AppState(DemoAccount.user, AppStartup.Ready), viewModel.state.value)
        auth.users.value = null
        runCurrent()
        assertEquals(AppState(user = null, startup = AppStartup.Ready), viewModel.state.value)
        viewModel.onEvent(AppCommand.RetryRestore)
        advanceUntilIdle()
        assertEquals(1, auth.restoreCalls)
    }

    @Test
    fun thrownRestoreErrorAlsoShowsRecoverableFailure() = runTest {
        val auth = StartupAuthRepository()
        auth.restore = { throw IOException("Offline") }
        val viewModel = AppViewModel(auth)

        advanceUntilIdle()

        assertEquals(AppStartup.Failed, viewModel.state.value.startup)
        assertNull(viewModel.state.value.user)
    }

    private class StartupAuthRepository : AuthRepository {
        val users = MutableStateFlow<CalendarUser?>(null)
        override val currentUser = users.asStateFlow()
        var restoreCalls = 0
        var logoutCalls = 0
        var restore: suspend () -> Result<Unit> = { Result.success(Unit) }
        var logoutAction: suspend () -> Result<Unit> = {
            users.value = null
            Result.success(Unit)
        }

        override suspend fun restoreSession(): Result<Unit> {
            restoreCalls++
            return restore()
        }

        override suspend fun logout(): Result<Unit> {
            logoutCalls++
            return logoutAction()
        }

        override suspend fun login(email: String, password: String): Result<CalendarUser> =
            Result.failure(UnsupportedOperationException())

        override suspend fun register(draft: RegistrationDraft): Result<CalendarUser> =
            Result.failure(UnsupportedOperationException())
    }
}
