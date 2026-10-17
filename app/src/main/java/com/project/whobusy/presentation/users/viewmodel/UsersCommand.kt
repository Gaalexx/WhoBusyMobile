package com.project.whobusy.presentation.users.viewmodel

sealed interface UsersCommand {
    data object Refresh : UsersCommand
}
