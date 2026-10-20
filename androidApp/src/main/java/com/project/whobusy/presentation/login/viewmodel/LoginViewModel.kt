package com.project.whobusy.presentation.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.repository.AuthRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class LoginViewModel(
    private val repository: AuthRepository,
    defaults: LoginDefaults,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState(defaults.email, defaults.password))
    val state = _state.asStateFlow()

    fun onEvent(command: LoginCommand) {
        if (_state.value.isLoading) return
        when (command) {
            is LoginCommand.EmailChanged ->
                _state.update {
                    it.copy(email = command.value, emailError = null, error = null)
                }

            is LoginCommand.PasswordChanged ->
                _state.update {
                    it.copy(password = command.value, passwordError = null, error = null)
                }

            LoginCommand.Submit -> login()
        }
    }

    private fun login() {
        val current = _state.value
        val emailError = if (current.email.isBlank()) R.string.auth_error_identifier else null
        val passwordError = if (current.password.isEmpty()) R.string.error_login_password else null
        _state.update { it.copy(emailError = emailError, passwordError = passwordError, error = null) }
        if (emailError != null || passwordError != null) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository
                .login(current.email, current.password)
                .onSuccess { _state.update { it.copy(isLoading = false, password = "") } }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error =
                                if (error is InvalidCredentialsException) {
                                    R.string.error_login_credentials
                                } else {
                                    R.string.error_login_connection
                                },
                        )
                    }
                }
        }
    }
}
