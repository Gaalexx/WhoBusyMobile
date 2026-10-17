package com.project.whobusy.presentation.calendar.viewmodel

import androidx.annotation.StringRes
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.presentation.calendar.model.CalendarMode
import java.time.LocalDate

data class CalendarState(
    val selectedDate: LocalDate,
    val mode: CalendarMode = CalendarMode.Day,
    val user: CalendarUser? = null,
    val users: List<CalendarUser> = emptyList(),
    val events: List<CalendarEvent> = emptyList(),
    val visibleOwnerIds: Set<Int> = emptySet(),
    val accessibleOwnerIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    @param:StringRes val error: Int? = null,
)
