package com.project.whobusy.presentation

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsCommand
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsViewModel
import com.project.whobusy.presentation.login.viewmodel.LoginCommand
import com.project.whobusy.presentation.login.viewmodel.LoginDefaults
import com.project.whobusy.presentation.login.viewmodel.LoginViewModel
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorViewModel
import com.project.whobusy.presentation.profile.viewmodel.ProfileViewModel
import com.project.whobusy.presentation.register.viewmodel.RegistrationCommand
import com.project.whobusy.presentation.register.viewmodel.RegistrationViewModel
import com.project.whobusy.presentation.users.viewmodel.UserEditorCommand
import com.project.whobusy.presentation.users.viewmodel.UserEditorViewModel
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.admin_error_last_admin
import com.project.whobusy.resources.auth_error_current_password
import com.project.whobusy.resources.auth_error_login_taken
import com.project.whobusy.resources.spaces_error_action
import com.project.whobusy.time.Clock
import com.project.whobusy.time.Instant
import com.project.whobusy.time.ZoneOffset
import com.project.whobusy.time.parse
import com.project.whobusy.time.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

            assertEquals(Res.string.auth_error_login_taken, viewModel.state.value.loginError)
            assertNull(session.getUser())

            viewModel.onEvent(RegistrationCommand.LoginChanged("new.user"))
            viewModel.onEvent(RegistrationCommand.Submit)
            advanceUntilIdle()
            assertEquals("new.user", session.getUser()?.login)
        }

    @Test
    fun editorKeepsFailedPasswordChangeOpenAndRetainsSavedUntilHandled() =
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

            assertEquals(Res.string.auth_error_current_password, viewModel.state.value.currentPasswordError)
            assertEquals(DemoAccount.user, session.getUser())
            assertEquals("Новое", viewModel.state.value.firstName)
            assertFalse(viewModel.state.value.isSaved)

            viewModel.onEvent(ProfileEditorCommand.CurrentPasswordChanged(DemoAccount.PASSWORD))
            viewModel.onEvent(ProfileEditorCommand.Save)
            advanceUntilIdle()

            assertTrue(viewModel.state.first().isSaved)
            assertEquals("Новое", session.getUser()?.firstName)
            assertNull(session.getUser()?.email)
            assertEquals(session.getUser(), accounts.authenticate("demo", "changed"))
            assertEquals("", viewModel.state.value.currentPassword)
            assertEquals("", viewModel.state.value.newPassword)

            val completedState = viewModel.state.value
            viewModel.onEvent(ProfileEditorCommand.FirstNameChanged("Не должно сохраниться"))
            viewModel.onEvent(ProfileEditorCommand.Save)
            advanceUntilIdle()
            assertEquals(completedState, viewModel.state.value)
            assertEquals("Новое", session.getUser()?.firstName)
            viewModel.onEvent(ProfileEditorCommand.ResultHandled)
            assertFalse(viewModel.state.value.isSaved)
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
            creator.onEvent(UserEditorCommand.Save)
            advanceUntilIdle()

            assertTrue(creator.state.first().isFinished)
            val created = accounts.authenticate("managed.user", "password")
            val completedState = creator.state.value
            creator.onEvent(UserEditorCommand.Save)
            advanceUntilIdle()
            assertEquals(completedState, creator.state.value)
            creator.onEvent(UserEditorCommand.ResultHandled)
            assertFalse(creator.state.value.isFinished)

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
            assertFalse(editor.state.value.isFinished)

            editor.onEvent(UserEditorCommand.RequestDelete)
            editor.onEvent(UserEditorCommand.ConfirmDelete)
            advanceUntilIdle()
            assertTrue(editor.state.first().isFinished)
            assertFalse(accounts.users.value.any { it.id == created.id })
            editor.onEvent(UserEditorCommand.Save)
            advanceUntilIdle()
            assertTrue(editor.state.value.isFinished)
            assertNull(editor.state.value.error)
            editor.onEvent(UserEditorCommand.ResultHandled)
            assertFalse(editor.state.value.isFinished)
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

            assertEquals(Res.string.admin_error_last_admin, editor.state.value.error)
            assertFalse(editor.state.value.isFinished)
            assertEquals(UserRole.ADMIN, session.getUser()?.role)
            assertEquals(UserRole.ADMIN, accounts.authenticate("admin", "admin123").role)
        }

    @Test
    fun boardSaveRetainsCompletionWithoutRootAndRequiresAcknowledgementBeforeAnotherSave() =
        runTest {
            auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
            val viewModel = BoardSettingsViewModel(workspace)
            viewModel.onEvent(BoardSettingsCommand.Load)
            advanceUntilIdle()
            viewModel.onEvent(BoardSettingsCommand.OnlyOwn)
            viewModel.onEvent(BoardSettingsCommand.Save)
            advanceUntilIdle()

            assertTrue(viewModel.state.first().isSaved)
            assertFalse(viewModel.state.value.isSaving)
            assertTrue(workspace.getBoardSettings().getOrThrow().spaceIds.isEmpty())
            val completedState = viewModel.state.value
            viewModel.onEvent(BoardSettingsCommand.AllSpaces)
            viewModel.onEvent(BoardSettingsCommand.Save)
            advanceUntilIdle()
            assertEquals(completedState, viewModel.state.value)

            viewModel.onEvent(BoardSettingsCommand.ResultHandled)
            assertFalse(viewModel.state.value.isSaved)
            viewModel.onEvent(BoardSettingsCommand.AllSpaces)
            viewModel.onEvent(BoardSettingsCommand.Save)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isSaved)
            assertEquals(setOf(1, 2, 3), workspace.getBoardSettings().getOrThrow().spaceIds)
        }

    @Test
    fun failedBoardSaveDoesNotSetCompletion() =
        runTest {
            auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
            val viewModel = BoardSettingsViewModel(workspace)
            viewModel.onEvent(BoardSettingsCommand.Load)
            advanceUntilIdle()
            auth.logout()

            viewModel.onEvent(BoardSettingsCommand.Save)
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isSaved)
            assertFalse(viewModel.state.value.isSaving)
            assertEquals(Res.string.spaces_error_action, viewModel.state.value.error)
        }
}
