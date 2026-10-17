package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.project.whobusy.R
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import java.time.format.DateTimeFormatter

@Composable
fun eventTime(event: CalendarEvent): String =
    if (event.allDay) {
        stringResource(R.string.label_all_day)
    } else {
        val format = DateTimeFormatter.ofPattern("HH:mm")
        stringResource(R.string.event_time_range, event.start.format(format), event.end.format(format))
    }

@Composable
fun statusTitle(status: EventStatus): String =
    stringResource(
        when (status) {
            EventStatus.BUSY -> R.string.event_status_busy
            EventStatus.FREE -> R.string.event_status_free
            EventStatus.TENTATIVE -> R.string.event_status_tentative
        },
    )
