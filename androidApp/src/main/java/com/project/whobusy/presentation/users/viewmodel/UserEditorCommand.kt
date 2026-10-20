package com.project.whobusy.presentation.users.viewmodel

import com.project.whobusy.domain.model.UserRole

sealed interface UserEditorCommand {
    data class Load(
        val userId: Int?,
    ) : UserEditorCommand

    data class LoginChanged(
        val value: String,
    ) : UserEditorCommand

    data class FirstNameChanged(
        val value: String,
    ) : UserEditorCommand

    data class LastNameChanged(
        val value: String,
    ) : UserEditorCommand

    data class EmailChanged(
        val value: String,
    ) : UserEditorCommand

    data class PasswordChanged(
        val value: String,
    ) : UserEditorCommand

    data class RoleChanged(
        val value: UserRole,
    ) : UserEditorCommand

    data class ColorChanged(
        val value: String,
    ) : UserEditorCommand

    data class ActiveChanged(
        val value: Boolean,
    ) : UserEditorCommand

    data object Save : UserEditorCommand

    data object RequestDelete : UserEditorCommand

    data object CancelDelete : UserEditorCommand

    data object ConfirmDelete : UserEditorCommand

    data object ResultHandled : UserEditorCommand
}
