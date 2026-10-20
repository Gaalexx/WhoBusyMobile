package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.board_for_actors
import com.project.whobusy.resources.board_hidden_details
import com.project.whobusy.resources.board_personal
import com.project.whobusy.resources.board_public
import com.project.whobusy.resources.event_status_busy
import com.project.whobusy.resources.event_status_free
import com.project.whobusy.resources.event_status_tentative
import com.project.whobusy.resources.event_time_range
import com.project.whobusy.resources.label_all_day
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.format
import org.jetbrains.compose.resources.stringResource

@Composable
fun eventVisibilitySummary(
    event: CalendarEvent,
    state: CalendarState,
): String =
    when {
        event.busyOnly -> stringResource(Res.string.board_hidden_details)
        event.spaceId != null ->
            stringResource(
                Res.string.board_public,
                state.spaces
                    .firstOrNull { it.id == event.spaceId }
                    ?.name
                    .orEmpty(),
            )

        event.shares.isNotEmpty() -> stringResource(Res.string.board_for_actors)
        else -> stringResource(Res.string.board_personal)
    }

@Composable
fun eventTime(event: CalendarEvent): String =
    if (event.allDay) {
        stringResource(Res.string.label_all_day)
    } else {
        val format = DateTimeFormatter.ofPattern("HH:mm")
        stringResource(Res.string.event_time_range, event.start.format(format), event.end.format(format))
    }

@Composable
fun statusTitle(status: EventStatus): String =
    stringResource(
        when (status) {
            EventStatus.BUSY -> Res.string.event_status_busy
            EventStatus.FREE -> Res.string.event_status_free
            EventStatus.TENTATIVE -> Res.string.event_status_tentative
        },
    )
