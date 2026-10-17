package com.project.whobusy.presentation.login.viewmodel

import androidx.annotation.StringRes

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val error: Int? = null,
)

data class LoginDefaults(
    val email: String = "",
    val password: String = "",
)
