package com.project.whobusy.presentation.calendar.viewmodel

import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.time.LocalDate
import org.jetbrains.compose.resources.StringResource

data class CalendarState(
    val selectedDate: LocalDate,
    val mode: CalendarMode = CalendarMode.Day,
    val user: CalendarUser? = null,
    val users: List<CalendarUser> = emptyList(),
    val events: List<CalendarEvent> = emptyList(),
    val visibleOwnerIds: Set<Int> = emptySet(),
    val accessibleOwnerIds: Set<Int> = emptySet(),
    val spaces: List<Space> = emptyList(),
    val boardSettings: BoardSettings = BoardSettings(emptySet()),
    val series: List<CalendarEvent> = emptyList(),
    val loadedStart: LocalDate? = null,
    val loadedEndExclusive: LocalDate? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val result: CalendarEffect? = null,
    val error: StringResource? = null,
)
