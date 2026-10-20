package com.project.whobusy.presentation

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.presentation.login.viewmodel.LoginCommand
import com.project.whobusy.presentation.login.viewmodel.LoginDefaults
import com.project.whobusy.presentation.login.viewmodel.LoginViewModel
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.error_login_credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validatesFieldsAndAllowsRetryAfterInvalidCredentials() =
        runTest {
            val session = SessionStore()
            val viewModel = LoginViewModel(MockAuthRepository(session), LoginDefaults())
            viewModel.onEvent(LoginCommand.Submit)
            assertNotNull(viewModel.state.value.emailError)
            assertNotNull(viewModel.state.value.passwordError)
            viewModel.onEvent(LoginCommand.EmailChanged(DemoAccount.EMAIL))
            viewModel.onEvent(LoginCommand.PasswordChanged("incorrect"))
            viewModel.onEvent(LoginCommand.Submit)
            advanceUntilIdle()
            assertEquals(Res.string.error_login_credentials, viewModel.state.value.error)
            assertNull(session.getUser())
            viewModel.onEvent(LoginCommand.PasswordChanged(DemoAccount.PASSWORD))
            viewModel.onEvent(LoginCommand.Submit)
            advanceUntilIdle()
            assertEquals(DemoAccount.user, session.getUser())
            assertEquals("", viewModel.state.value.password)
        }
}
