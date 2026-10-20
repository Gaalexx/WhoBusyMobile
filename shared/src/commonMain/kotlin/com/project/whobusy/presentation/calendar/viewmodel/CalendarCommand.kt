package com.project.whobusy.presentation.calendar.viewmodel

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.time.LocalDate

sealed interface CalendarCommand {
    data class SelectDate(
        val date: LocalDate,
    ) : CalendarCommand

    data class SelectMode(
        val mode: CalendarMode,
    ) : CalendarCommand

    data class MovePeriod(
        val offset: Long,
    ) : CalendarCommand

    data class ToggleOwner(
        val id: Int,
    ) : CalendarCommand

    data class OpenEvent(
        val event: CalendarEvent,
    ) : CalendarCommand

    data class SaveEvent(
        val draft: EventDraft,
        val eventId: Int? = null,
    ) : CalendarCommand

    data class DeleteEvent(
        val eventId: Int,
    ) : CalendarCommand

    data object Today : CalendarCommand

    data object Refresh : CalendarCommand

    data object CreateEvent : CalendarCommand

    data object OpenCalendars : CalendarCommand

    data object ClearError : CalendarCommand

    data object ResultHandled : CalendarCommand

    data object Logout : CalendarCommand
}
