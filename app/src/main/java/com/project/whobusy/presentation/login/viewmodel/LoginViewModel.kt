package com.project.whobusy.presentation.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
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
            val emailError =
                if (Regex(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
                    ).matches(current.email.trim())
                ) {
                    null
                } else {
                    "Введите корректный email"
                }
            val passwordError = if (current.password.isBlank()) "Введите пароль" else null
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
                                        "Неверный email или пароль"
                                    } else {
                                        "Не удалось войти. Проверьте подключение и попробуйте снова."
                                    },
                            )
                        }
                    }
            }
        }
    }
