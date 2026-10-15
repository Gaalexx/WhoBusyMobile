package com.project.whobusy.presentation.calendar.viewmodel

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.CalendarTab
import java.time.LocalDate

sealed interface CalendarCommand {
    data class SelectDate(val date: LocalDate) : CalendarCommand
    data class SelectMode(val mode: CalendarMode) : CalendarCommand
    data class SelectTab(val tab: CalendarTab) : CalendarCommand
    data class MovePeriod(val offset: Long) : CalendarCommand
    data class ToggleOwner(val id: Int) : CalendarCommand
    data class OpenEvent(val event: CalendarEvent) : CalendarCommand
    data class SaveEvent(val draft: EventDraft) : CalendarCommand
    data object Today : CalendarCommand
    data object Refresh : CalendarCommand
    data object CreateEvent : CalendarCommand
    data object EditEvent : CalendarCommand
    data object DeleteEvent : CalendarCommand
    data object CloseEvent : CalendarCommand
    data object Logout : CalendarCommand
}
