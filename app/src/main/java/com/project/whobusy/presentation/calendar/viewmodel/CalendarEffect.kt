package com.project.whobusy.presentation.calendar.viewmodel

sealed interface CalendarEffect {
    data class OpenEvent(
        val eventId: Int,
    ) : CalendarEffect

    data class CreateEvent(
        val date: String,
    ) : CalendarEffect

    data class EditEvent(
        val eventId: Int,
    ) : CalendarEffect

    data object OpenCalendars : CalendarEffect

    data object EventSaved : CalendarEffect

    data object EventDeleted : CalendarEffect
}
