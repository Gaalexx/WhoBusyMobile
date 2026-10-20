package com.project.whobusy.presentation.register.viewmodel

import androidx.annotation.StringRes

data class RegistrationState(
    val login: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @param:StringRes val loginError: Int? = null,
    @param:StringRes val firstNameError: Int? = null,
    @param:StringRes val lastNameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val error: Int? = null,
)
