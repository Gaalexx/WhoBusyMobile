package com.project.whobusy.presentation.login.viewmodel

sealed interface LoginCommand {
    data class EmailChanged(val value: String) : LoginCommand
    data class PasswordChanged(val value: String) : LoginCommand
    data object Submit : LoginCommand
}
