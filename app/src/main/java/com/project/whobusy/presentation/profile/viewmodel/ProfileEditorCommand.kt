package com.project.whobusy.presentation.profile.viewmodel

sealed interface ProfileEditorCommand {
    data class FirstNameChanged(
        val value: String,
    ) : ProfileEditorCommand

    data class LastNameChanged(
        val value: String,
    ) : ProfileEditorCommand

    data class EmailChanged(
        val value: String,
    ) : ProfileEditorCommand

    data class ColorChanged(
        val value: String,
    ) : ProfileEditorCommand

    data class CurrentPasswordChanged(
        val value: String,
    ) : ProfileEditorCommand

    data class NewPasswordChanged(
        val value: String,
    ) : ProfileEditorCommand

    data object Save : ProfileEditorCommand

    data object ResultHandled : ProfileEditorCommand
}
