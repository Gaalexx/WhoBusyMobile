package com.project.whobusy.presentation.board.viewmodel

import androidx.annotation.StringRes
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.Space

data class BoardSettingsState(
    val spaces: List<Space> = emptyList(),
    val settings: BoardSettings = BoardSettings(emptySet()),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    @param:StringRes val error: Int? = null,
    val isSaved: Boolean = false,
)
