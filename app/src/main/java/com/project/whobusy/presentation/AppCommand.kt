package com.project.whobusy.presentation

sealed interface AppCommand {
    data object RetryRestore : AppCommand
    data object SignInAgain : AppCommand
}
