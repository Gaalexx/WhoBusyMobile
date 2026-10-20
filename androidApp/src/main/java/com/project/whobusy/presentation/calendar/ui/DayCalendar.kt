package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyEventCard
import com.project.whobusy.core.ui.components.WhoBusyTimeGrid
import com.project.whobusy.core.ui.components.WhoBusyTimeGridItem
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.placeEvents
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import java.time.LocalDateTime

@Composable
fun DayCalendar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    now: LocalDateTime,
) {
    val events = remember(state.events, state.selectedDate) { eventsOnDate(state.events, state.selectedDate) }
    val placements = remember(events, state.selectedDate) { placeEvents(events, state.selectedDate) }
    val allDay = remember(events) { events.filter { it.allDay } }
    val density = LocalDensity.current
    val titleHeight = with(density) { MaterialTheme.typography.titleMedium.lineHeight.toDp() } + 4.dp
    val subtitleHeight = titleHeight + with(density) { MaterialTheme.typography.bodySmall.lineHeight.toDp() }
    val firstHour = placements.minOfOrNull { it.startMinute / 60 }?.coerceAtMost(9) ?: 9
    Column(Modifier.fillMaxSize()) {
        key(state.selectedDate) { DayAllDayEvents(allDay, onEvent) }
        WhoBusyTimeGrid(
            hourHeight = 64.dp,
            timeColumnWidth = 60.dp,
            initialHour = firstHour,
            resetKey = state.selectedDate,
            hourLabel = { "%02d:00".format(it) },
            modifier = Modifier.weight(1f).testTag("day-time-grid"),
            currentMinute = now.takeIf { it.toLocalDate() == state.selectedDate }?.let { it.hour * 60 + it.minute },
            currentTimeLabel = "%02d:%02d".format(now.hour, now.minute),
        ) { viewport ->
            val eventWidth = maxWidth - 76.dp
            placements.filter { viewport.isVisible(it.startMinute, it.endMinute) }.forEach { placement ->
                val event = placement.event
                val width = eventWidth / placement.laneCount
                key(event.id, event.start) {
                    WhoBusyTimeGridItem(
                        viewport = viewport,
                        startMinute = placement.startMinute,
                        endMinute = placement.endMinute,
                        left = 64.dp + width * placement.lane,
                        width = width - 4.dp,
                    ) {
                        val height =
                            with(density) { viewport.eventHeight(placement.startMinute, placement.endMinute).toDp() }
                        val owner = if (event.ownerId == state.user?.id) {
                            stringResource(R.string.label_you)
                        } else {
                            event.ownerName.substringBefore(" ")
                        }
                        WhoBusyEventCard(
                            title = event.title,
                            subtitle = if (height >= subtitleHeight) {
                                stringResource(R.string.board_time_owner, eventTime(event), owner)
                            } else null,
                            color = eventColor(event),
                            onClick = { onEvent(CalendarCommand.OpenEvent(event)) },
                            modifier = Modifier.fillMaxSize().testTag("day-event-${event.id}"),
                            detail = if (height >= 64.dp) eventVisibilitySummary(event, state) else null,
                            compact = height < 82.dp,
                            showText = height >= titleHeight,
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 500, showBackground = true)
@Composable
private fun DayCalendarPreview() {
    WhoBusyTheme { Column { DayCalendar(CalendarPreviewData.state, {}, CalendarPreviewData.date.atTime(11, 45)) } }
}

@Preview(name = "Короткие и многочасовые события", widthDp = 390, heightDp = 500, showBackground = true)
@Composable
private fun DayCalendarDurationsPreview() {
    val date = CalendarPreviewData.date
    val event = CalendarPreviewData.events.first()
    val events = listOf(
        event.copy(id = 1, start = date.atTime(9, 0), end = date.atTime(9, 5)),
        event.copy(id = 2, start = date.atTime(9, 15), end = date.atTime(9, 30)),
        event.copy(id = 3, start = date.atTime(10, 0), end = date.atTime(11, 0)),
        event.copy(id = 4, start = date.atTime(12, 0), end = date.atTime(23, 0)),
    )
    WhoBusyTheme { Column { DayCalendar(CalendarPreviewData.state.copy(events = events), {}, date.atTime(10, 30)) } }
}
