package com.project.whobusy.presentation.navigation

import com.project.whobusy.domain.model.CalendarEvent

fun CalendarEvent.detailsRoute(): ScreenTypes.EventDetails =
    ScreenTypes.EventDetails(eventId = id, occurrenceStart = start.toString())
