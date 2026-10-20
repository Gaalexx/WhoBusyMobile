package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.project.whobusy.R
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import java.time.format.DateTimeFormatter

@Composable
fun eventVisibilitySummary(
    event: CalendarEvent,
    state: CalendarState,
): String =
    when {
        event.busyOnly -> stringResource(R.string.board_hidden_details)
        event.spaceId != null ->
            stringResource(
                R.string.board_public,
                state.spaces
                    .firstOrNull { it.id == event.spaceId }
                    ?.name
                    .orEmpty(),
            )

        event.shares.isNotEmpty() -> stringResource(R.string.board_for_actors)
        else -> stringResource(R.string.board_personal)
    }

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
