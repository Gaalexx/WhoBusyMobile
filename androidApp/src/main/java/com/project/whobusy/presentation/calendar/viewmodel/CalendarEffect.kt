package com.project.whobusy.presentation.calendar.viewmodel

sealed interface CalendarEffect {
    data class EventSaved(
        val eventId: Int?,
    ) : CalendarEffect

    data class EventDeleted(
        val eventId: Int,
    ) : CalendarEffect
}
