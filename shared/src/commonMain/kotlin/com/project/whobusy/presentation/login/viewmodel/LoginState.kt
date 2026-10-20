package com.project.whobusy.presentation.login.viewmodel

import org.jetbrains.compose.resources.StringResource

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: StringResource? = null,
    val passwordError: StringResource? = null,
    val error: StringResource? = null,
)

data class LoginDefaults(
    val email: String = "",
    val password: String = "",
)
