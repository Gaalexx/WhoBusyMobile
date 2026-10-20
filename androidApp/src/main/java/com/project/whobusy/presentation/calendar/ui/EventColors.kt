package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarEvent

@Composable
fun eventColor(event: CalendarEvent): Color =
    if (event.busyOnly) WhoBusyColors.SecondaryText else calendarColor(event.displayColor)
