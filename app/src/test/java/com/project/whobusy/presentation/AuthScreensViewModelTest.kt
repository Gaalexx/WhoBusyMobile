package com.project.whobusy.presentation

import com.project.whobusy.R
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.login.viewmodel.LoginCommand
import com.project.whobusy.presentation.login.viewmodel.LoginDefaults
import com.project.whobusy.presentation.login.viewmodel.LoginViewModel
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorEffect
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorViewModel
import com.project.whobusy.presentation.profile.viewmodel.ProfileViewModel
import com.project.whobusy.presentation.register.viewmodel.RegistrationCommand
import com.project.whobusy.presentation.register.viewmodel.RegistrationViewModel
import com.project.whobusy.presentation.users.viewmodel.UserEditorCommand
import com.project.whobusy.presentation.users.viewmodel.UserEditorEffect
import com.project.whobusy.presentation.users.viewmodel.UserEditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AuthScreensViewModelTest {
    private val session = SessionStore()
    private val accounts = MockAccountStore()
    private val auth = MockAuthRepository(session, accounts)
    private val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
    private val workspace = MockWorkspaceRepository(session, accounts, clock)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginAcceptsUsernameWithoutAnEmailAddress() =
        runTest {
            val viewModel = LoginViewModel(auth, LoginDefaults())
            viewModel.onEvent(LoginCommand.EmailChanged(" DEMO "))
            viewModel.onEvent(LoginCommand.PasswordChanged(DemoAccount.PASSWORD))
            viewModel.onEvent(LoginCommand.Submit)
            advanceUntilIdle()

            assertEquals(DemoAccount.user, session.getUser())
            assertNull(viewModel.state.value.emailError)
        }

    @Test
    fun registrationValidatesRequiredFieldsThenSignsInWithOptionalEmailEmpty() =
        runTest {
            val viewModel = RegistrationViewModel(auth)
            viewModel.onEvent(RegistrationCommand.Submit)
            assertNotNull(viewModel.state.value.loginError)
            assertNotNull(viewModel.state.value.firstNameError)
            assertNotNull(viewModel.state.value.lastNameError)
            assertNotNull(viewModel.state.value.passwordError)
            assertNull(viewModel.state.value.emailError)

            viewModel.onEvent(RegistrationCommand.LoginChanged(" New.User "))
            viewModel.onEvent(RegistrationCommand.FirstNameChanged(" Елена "))
            viewModel.onEvent(RegistrationCommand.LastNameChanged(" Петрова "))
            viewModel.onEvent(RegistrationCommand.PasswordChanged("password"))
            viewModel.onEvent(RegistrationCommand.Submit)
            advanceUntilIdle()

            assertEquals("new.user", session.getUser()?.login)
            assertEquals("Петрова Елена", session.getUser()?.fullName)
            assertNull(session.getUser()?.email)
            assertEquals("", viewModel.state.value.password)
        }

    @Test
    fun registrationShowsDuplicateLoginAndAllowsRetry() =
        runTest {
            val viewModel = RegistrationViewModel(auth)
            viewModel.onEvent(RegistrationCommand.LoginChanged("demo"))
            viewModel.onEvent(RegistrationCommand.FirstNameChanged("Елена"))
            viewModel.onEvent(RegistrationCommand.LastNameChanged("Петрова"))
            viewModel.onEvent(RegistrationCommand.PasswordChanged("password"))
            viewModel.onEvent(RegistrationCommand.Submit)
            advanceUntilIdle()

            assertEquals(R.string.auth_error_login_taken, viewModel.state.value.loginError)
            assertNull(session.getUser())

            viewModel.onEvent(RegistrationCommand.LoginChanged("new.user"))
            viewModel.onEvent(RegistrationCommand.Submit)
            advanceUntilIdle()
            assertEquals("new.user", session.getUser()?.login)
        }

    @Test
    fun editorKeepsFailedPasswordChangeOpenAndEmitsSavedOnlyAfterSuccessfulRetry() =
        runTest {
            auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
            val viewModel = ProfileEditorViewModel(auth, workspace)
            advanceUntilIdle()
            assertEquals("demo", viewModel.state.value.login)
            viewModel.onEvent(ProfileEditorCommand.FirstNameChanged("Новое"))
            viewModel.onEvent(ProfileEditorCommand.EmailChanged(""))
            viewModel.onEvent(ProfileEditorCommand.NewPasswordChanged("changed"))
            viewModel.onEvent(ProfileEditorCommand.CurrentPasswordChanged("incorrect"))
            viewModel.onEvent(ProfileEditorCommand.Save)
            advanceUntilIdle()

            assertEquals(R.string.auth_error_current_password, viewModel.state.value.currentPasswordError)
            assertEquals(DemoAccount.user, session.getUser())
            assertEquals("Новое", viewModel.state.value.firstName)

            val effect = async { viewModel.effects.first() }
            viewModel.onEvent(ProfileEditorCommand.CurrentPasswordChanged(DemoAccount.PASSWORD))
            viewModel.onEvent(ProfileEditorCommand.Save)
            advanceUntilIdle()

            assertEquals(ProfileEditorEffect.Saved, effect.await())
            assertEquals("Новое", session.getUser()?.firstName)
            assertNull(session.getUser()?.email)
            assertEquals(session.getUser(), accounts.authenticate("demo", "changed"))
            assertEquals("", viewModel.state.value.currentPassword)
            assertEquals("", viewModel.state.value.newPassword)
        }

    @Test
    fun profileLoadsInvitationCountAndLogoutClearsSession() =
        runTest {
            auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
            val expectedCount = workspace.getInvitations().getOrThrow().size
            val viewModel = ProfileViewModel(auth, workspace)
            viewModel.onEvent(ProfileCommand.Refresh)
            advanceUntilIdle()

            assertEquals(DemoAccount.user, viewModel.state.value.user)
            assertEquals(expectedCount, viewModel.state.value.invitationCount)

            viewModel.onEvent(ProfileCommand.Logout)
            advanceUntilIdle()
            assertNull(session.getUser())
            assertNull(viewModel.state.value.user)
        }

    @Test
    fun adminCreatesAccountAndDeletionRequiresAnOpenConfirmation() =
        runTest {
            auth.login("admin", "admin123").getOrThrow()
            val creator = UserEditorViewModel(auth, workspace)
            creator.onEvent(UserEditorCommand.Load(null))
            advanceUntilIdle()
            creator.onEvent(UserEditorCommand.LoginChanged("managed.user"))
            creator.onEvent(UserEditorCommand.FirstNameChanged("Новый"))
            creator.onEvent(UserEditorCommand.PasswordChanged("password"))
            val saved = async { creator.effects.first() }
            creator.onEvent(UserEditorCommand.Save)
            advanceUntilIdle()

            assertEquals(UserEditorEffect.Saved, saved.await())
            val created = accounts.authenticate("managed.user", "password")
            val editor = UserEditorViewModel(auth, workspace)
            editor.onEvent(UserEditorCommand.Load(created.id))
            advanceUntilIdle()
            assertTrue(editor.state.value.canDelete)
            editor.onEvent(UserEditorCommand.RequestDelete)
            assertTrue(editor.state.value.showDeleteConfirmation)
            editor.onEvent(UserEditorCommand.CancelDelete)
            editor.onEvent(UserEditorCommand.ConfirmDelete)
            advanceUntilIdle()
            assertTrue(accounts.users.value.any { it.id == created.id })

            val deleted = async { editor.effects.first() }
            editor.onEvent(UserEditorCommand.RequestDelete)
            editor.onEvent(UserEditorCommand.ConfirmDelete)
            advanceUntilIdle()
            assertEquals(UserEditorEffect.Deleted, deleted.await())
            assertFalse(accounts.users.value.any { it.id == created.id })
        }

    @Test
    fun editorCannotDeleteSelfOrDemoteTheLastActiveAdmin() =
        runTest {
            auth.login("admin", "admin123").getOrThrow()
            val editor = UserEditorViewModel(auth, workspace)
            editor.onEvent(UserEditorCommand.Load(100))
            advanceUntilIdle()
            assertFalse(editor.state.value.canDelete)
            editor.onEvent(UserEditorCommand.RequestDelete)
            editor.onEvent(UserEditorCommand.ConfirmDelete)
            assertFalse(editor.state.value.showDeleteConfirmation)

            editor.onEvent(UserEditorCommand.RoleChanged(UserRole.USER))
            editor.onEvent(UserEditorCommand.Save)
            advanceUntilIdle()

            assertEquals(R.string.admin_error_last_admin, editor.state.value.error)
            assertEquals(UserRole.ADMIN, session.getUser()?.role)
            assertEquals(UserRole.ADMIN, accounts.authenticate("admin", "admin123").role)
        }
}
