package com.project.whobusy.presentation.register.viewmodel

import org.jetbrains.compose.resources.StringResource

data class RegistrationState(
    val login: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginError: StringResource? = null,
    val firstNameError: StringResource? = null,
    val lastNameError: StringResource? = null,
    val emailError: StringResource? = null,
    val passwordError: StringResource? = null,
    val error: StringResource? = null,
)
