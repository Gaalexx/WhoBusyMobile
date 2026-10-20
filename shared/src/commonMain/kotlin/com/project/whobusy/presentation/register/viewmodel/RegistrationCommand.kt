package com.project.whobusy.presentation.register.viewmodel

sealed interface RegistrationCommand {
    data class LoginChanged(
        val value: String,
    ) : RegistrationCommand

    data class FirstNameChanged(
        val value: String,
    ) : RegistrationCommand

    data class LastNameChanged(
        val value: String,
    ) : RegistrationCommand

    data class EmailChanged(
        val value: String,
    ) : RegistrationCommand

    data class PasswordChanged(
        val value: String,
    ) : RegistrationCommand

    data object Submit : RegistrationCommand
}
