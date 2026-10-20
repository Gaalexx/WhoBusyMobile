package com.project.whobusy.presentation.profile.viewmodel

sealed interface ProfileCommand {
    data object Refresh : ProfileCommand

    data object Logout : ProfileCommand

    data object RequestLogoutAll : ProfileCommand

    data object CancelLogoutAll : ProfileCommand

    data object ConfirmLogoutAll : ProfileCommand
}
