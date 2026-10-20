package com.project.whobusy.presentation.event.viewmodel

import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.event.model.EventFormState
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.of
import org.jetbrains.compose.resources.StringResource

data class EditorState(
    val form: EventFormState = EventFormState.initial(LocalDate.of(2026, 10, 17), null),
    val eventId: Int? = null,
    val spaces: List<Space> = emptyList(),
    val canChangeVisibility: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: StringResource? = null,
)
