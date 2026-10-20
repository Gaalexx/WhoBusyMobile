package com.project.whobusy.presentation.board.viewmodel

import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.Space
import org.jetbrains.compose.resources.StringResource

data class BoardSettingsState(
    val spaces: List<Space> = emptyList(),
    val settings: BoardSettings = BoardSettings(emptySet()),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: StringResource? = null,
    val isSaved: Boolean = false,
)
