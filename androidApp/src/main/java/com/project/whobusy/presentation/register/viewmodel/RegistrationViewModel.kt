package com.project.whobusy.presentation.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.model.RegistrationDraft
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
class RegistrationViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(RegistrationState())
    val state = mutableState.asStateFlow()

    fun onEvent(command: RegistrationCommand) {
        if (state.value.isLoading) return
        when (command) {
            is RegistrationCommand.LoginChanged ->
                mutableState.update { it.copy(login = command.value, loginError = null, error = null) }

            is RegistrationCommand.FirstNameChanged ->
                mutableState.update { it.copy(firstName = command.value, firstNameError = null, error = null) }

            is RegistrationCommand.LastNameChanged ->
                mutableState.update { it.copy(lastName = command.value, lastNameError = null, error = null) }

            is RegistrationCommand.EmailChanged ->
                mutableState.update { it.copy(email = command.value, emailError = null, error = null) }

            is RegistrationCommand.PasswordChanged ->
                mutableState.update { it.copy(password = command.value, passwordError = null, error = null) }

            RegistrationCommand.Submit -> register()
        }
    }

    private fun register() {
        val current = state.value
        val checked =
            current.copy(
                loginError =
                    if (LOGIN_PATTERN.matches(current.login.trim())) null else R.string.auth_error_login_format,
                firstNameError =
                    if (current.firstName.trim().length in 1..100) null else R.string.auth_error_name,
                lastNameError =
                    if (current.lastName.trim().length in 1..100) null else R.string.auth_error_name,
                emailError =
                    if (current.email.isBlank() || EMAIL_PATTERN.matches(current.email.trim())) {
                        null
                    } else {
                        R.string.error_login_email
                    },
                passwordError =
                    if (current.password.length in 4..128) null else R.string.auth_error_password_length,
                error = null,
            )
        mutableState.value = checked
        if (listOf(
                checked.loginError,
                checked.firstNameError,
                checked.lastNameError,
                checked.emailError,
                checked.passwordError,
            ).any { it != null }
        ) {
            return
        }
        mutableState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository
                .register(
                    RegistrationDraft(
                        login = current.login.trim(),
                        firstName = current.firstName.trim(),
                        lastName = current.lastName.trim(),
                        password = current.password,
                        email = current.email.trim().takeIf { it.isNotEmpty() },
                    ),
                ).onSuccess {
                    mutableState.update { it.copy(isLoading = false, password = "") }
                }.onFailure { error ->
                    mutableState.update {
                        when (error.message) {
                            "Login already taken" ->
                                it.copy(
                                    isLoading = false,
                                    loginError = R.string.auth_error_login_taken,
                                )

                            "Email already registered" ->
                                it.copy(isLoading = false, emailError = R.string.auth_error_email_taken)

                            else -> it.copy(isLoading = false, error = R.string.auth_error_registration)
                        }
                    }
                }
        }
    }

    private companion object {
        val LOGIN_PATTERN = Regex("[a-zA-Z0-9._-]{3,32}")
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
