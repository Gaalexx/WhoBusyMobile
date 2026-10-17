package com.project.whobusy.presentation

import com.project.whobusy.R
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.SpaceInvitation
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun globalLogoutRequiresAnOpenConfirmationAndCancelDoesNotCallAuth() = runTest {
        val auth = LogoutAuthRepository()
        val viewModel = ProfileViewModel(auth, workspace())
        advanceUntilIdle()

        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        advanceUntilIdle()
        assertEquals(0, auth.logoutAllCalls)
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        assertTrue(viewModel.state.value.showLogoutAllConfirmation)
        assertFalse(viewModel.state.value.isLoggingOut)
        viewModel.onEvent(ProfileCommand.CancelLogoutAll)
        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showLogoutAllConfirmation)
        assertEquals(0, auth.logoutAllCalls)
        assertEquals(0, auth.logoutCalls)
        assertEquals(DemoAccount.user, viewModel.state.value.user)
    }

    @Test
    fun confirmedGlobalLogoutWaitsForAuthKeepsModalAndIgnoresRepeatedActions() = runTest {
        val auth = LogoutAuthRepository()
        val completing = CompletableDeferred<Unit>()
        auth.logoutAllAction = {
            completing.await()
            auth.users.value = null
            Result.success(Unit)
        }
        var invitationLoads = 0
        val workspace = object : WorkspaceRepository by workspace() {
            override suspend fun getInvitations(): Result<List<SpaceInvitation>> {
                invitationLoads++
                return Result.success(emptyList())
            }
        }
        val viewModel = ProfileViewModel(auth, workspace)
        advanceUntilIdle()
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        assertTrue(viewModel.state.value.isLoggingOut)
        runCurrent()

        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        viewModel.onEvent(ProfileCommand.CancelLogoutAll)
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        viewModel.onEvent(ProfileCommand.Logout)
        viewModel.onEvent(ProfileCommand.Refresh)
        runCurrent()

        assertTrue(viewModel.state.value.showLogoutAllConfirmation)
        assertTrue(viewModel.state.value.isLoggingOut)
        assertEquals(DemoAccount.user, viewModel.state.value.user)
        assertEquals(1, auth.logoutAllCalls)
        assertEquals(0, auth.logoutCalls)
        assertEquals(0, invitationLoads)

        completing.complete(Unit)
        advanceUntilIdle()

        assertNull(viewModel.state.value.user)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.showLogoutAllConfirmation)
        assertFalse(viewModel.state.value.isLoggingOut)
    }

    @Test
    fun failedGlobalLogoutKeepsAuthenticatedProfileAndCanBeConfirmedAgain() = runTest {
        val auth = LogoutAuthRepository()
        auth.logoutAllAction = { Result.failure(IOException("Offline")) }
        val viewModel = ProfileViewModel(auth, workspace())
        advanceUntilIdle()
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        advanceUntilIdle()

        assertEquals(DemoAccount.user, viewModel.state.value.user)
        assertEquals(R.string.auth_error_logout_all, viewModel.state.value.error)
        assertFalse(viewModel.state.value.showLogoutAllConfirmation)
        assertFalse(viewModel.state.value.isLoggingOut)
        assertEquals(0, auth.logoutCalls)

        auth.logoutAllAction = {
            auth.users.value = null
            Result.success(Unit)
        }
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        assertNull(viewModel.state.value.error)
        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        advanceUntilIdle()

        assertEquals(2, auth.logoutAllCalls)
        assertNull(viewModel.state.value.user)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun ordinaryLogoutNeedsNoConfirmationAndDoesNotUseGlobalLogout() = runTest {
        val auth = LogoutAuthRepository()
        val completing = CompletableDeferred<Unit>()
        auth.logoutAction = {
            completing.await()
            auth.users.value = null
            Result.success(Unit)
        }
        val viewModel = ProfileViewModel(auth, workspace())
        advanceUntilIdle()
        viewModel.onEvent(ProfileCommand.Logout)
        runCurrent()
        viewModel.onEvent(ProfileCommand.Logout)
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        runCurrent()

        assertTrue(viewModel.state.value.isLoggingOut)
        assertFalse(viewModel.state.value.showLogoutAllConfirmation)
        assertEquals(1, auth.logoutCalls)
        assertEquals(0, auth.logoutAllCalls)
        assertEquals(DemoAccount.user, viewModel.state.value.user)
        completing.complete(Unit)
        advanceUntilIdle()

        assertNull(viewModel.state.value.user)
        assertFalse(viewModel.state.value.isLoggingOut)
    }

    @Test
    fun failedOrdinaryLogoutLeavesUserAndShowsResourceError() = runTest {
        val auth = LogoutAuthRepository()
        auth.logoutAction = { Result.failure(IOException("Storage unavailable")) }
        val viewModel = ProfileViewModel(auth, workspace())
        advanceUntilIdle()
        viewModel.onEvent(ProfileCommand.Logout)
        advanceUntilIdle()

        assertEquals(DemoAccount.user, viewModel.state.value.user)
        assertEquals(R.string.auth_error_logout, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoggingOut)
        assertEquals(0, auth.logoutAllCalls)
    }

    @Test
    fun logoutCancelsPendingInvitationRefreshSoItCannotReplaceLogoutError() = runTest {
        val auth = LogoutAuthRepository()
        auth.logoutAllAction = { Result.failure(IOException("Offline")) }
        val invitations = CompletableDeferred<Result<List<SpaceInvitation>>>()
        var refreshCancelled = false
        val workspace = object : WorkspaceRepository by workspace() {
            override suspend fun getInvitations(): Result<List<SpaceInvitation>> = try {
                invitations.await()
            } finally {
                refreshCancelled = true
            }
        }
        val viewModel = ProfileViewModel(auth, workspace)
        advanceUntilIdle()
        viewModel.onEvent(ProfileCommand.Refresh)
        runCurrent()
        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        advanceUntilIdle()
        invitations.complete(Result.failure(IOException("Late invitation error")))
        advanceUntilIdle()

        assertTrue(refreshCancelled)
        assertEquals(R.string.auth_error_logout_all, viewModel.state.value.error)
        assertEquals(DemoAccount.user, viewModel.state.value.user)
    }

    @Test
    fun guestCannotStartEitherLogoutOperation() = runTest {
        val auth = LogoutAuthRepository()
        auth.users.value = null
        val viewModel = ProfileViewModel(auth, workspace())
        advanceUntilIdle()

        viewModel.onEvent(ProfileCommand.RequestLogoutAll)
        viewModel.onEvent(ProfileCommand.ConfirmLogoutAll)
        viewModel.onEvent(ProfileCommand.Logout)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showLogoutAllConfirmation)
        assertFalse(viewModel.state.value.isLoggingOut)
        assertEquals(0, auth.logoutCalls)
        assertEquals(0, auth.logoutAllCalls)
    }

    private fun workspace(): WorkspaceRepository {
        val sessions = SessionStore().apply { save("mock-session", DemoAccount.user) }
        return MockWorkspaceRepository(sessions, MockAccountStore(), Clock.systemUTC())
    }

    private class LogoutAuthRepository : AuthRepository {
        val users = MutableStateFlow<CalendarUser?>(DemoAccount.user)
        override val currentUser = users.asStateFlow()
        var logoutCalls = 0
        var logoutAllCalls = 0
        var logoutAction: suspend () -> Result<Unit> = {
            users.value = null
            Result.success(Unit)
        }
        var logoutAllAction: suspend () -> Result<Unit> = {
            users.value = null
            Result.success(Unit)
        }

        override suspend fun login(email: String, password: String): Result<CalendarUser> =
            Result.failure(UnsupportedOperationException())

        override suspend fun logout(): Result<Unit> {
            logoutCalls++
            return logoutAction()
        }

        override suspend fun logoutAll(): Result<Unit> {
            logoutAllCalls++
            return logoutAllAction()
        }
    }
}
