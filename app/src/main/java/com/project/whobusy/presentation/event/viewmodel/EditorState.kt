package com.project.whobusy.presentation.event.viewmodel

import androidx.annotation.StringRes
import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.event.model.EventFormState
import java.time.LocalDate

data class EditorState(
    val form: EventFormState = EventFormState.initial(LocalDate.of(2026, 10, 17), null),
    val eventId: Int? = null,
    val spaces: List<Space> = emptyList(),
    val canChangeVisibility: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    @param:StringRes val error: Int? = null,
)

sealed interface EditorEffect {
    data object Saved : EditorEffect
}
