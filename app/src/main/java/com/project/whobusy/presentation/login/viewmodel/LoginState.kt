package com.project.whobusy.presentation.login.viewmodel

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
)

data class LoginDefaults(val email: String = "", val password: String = "")
