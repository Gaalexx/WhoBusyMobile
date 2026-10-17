package com.project.whobusy.presentation.board.viewmodel

sealed interface BoardSettingsCommand {
    data object Load : BoardSettingsCommand

    data object Save : BoardSettingsCommand

    data object ResultHandled : BoardSettingsCommand

    data object OnlyOwn : BoardSettingsCommand

    data object AllSpaces : BoardSettingsCommand

    data class ShowOwn(
        val value: Boolean,
    ) : BoardSettingsCommand

    data class ToggleSpace(
        val spaceId: Int,
    ) : BoardSettingsCommand
}
