package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyEventCard
import com.project.whobusy.core.ui.components.WhoBusyEventChip
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.presentation.calendar.model.eventTime
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.placeEvents
import com.project.whobusy.presentation.calendar.model.statusTitle
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
    val events = eventsOnDate(state.events, state.selectedDate)
    val allDay = events.filter { it.allDay }
    allDay.forEach { event ->
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                "Весь день",
                Modifier.width(80.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WhoBusyEventChip(
                "${event.ownerName.substringBefore(" ")} · ${event.title}",
                calendarColor(event.displayColor),
                { onEvent(CalendarCommand.OpenEvent(event)) },
                Modifier.weight(1f),
            )
        }
    }
    val density = LocalDensity.current
    val hourHeight = 64.dp
    val scroll = rememberScrollState()
    LaunchedEffect(state.selectedDate) {
        val firstHour = events.filterNot { it.allDay }.minOfOrNull { it.start.hour }?.coerceAtMost(9) ?: 9
        scroll.scrollTo(with(density) { (hourHeight * firstHour).roundToPx() })
    }
    BoxWithConstraints(Modifier.fillMaxSize().verticalScroll(scroll)) {
        val eventWidth = maxWidth - 76.dp
        Box(
            Modifier.fillMaxWidth().height(
                hourHeight * 24 + 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            repeat(24) { hour ->
                Row(
                    Modifier.offset(y = hourHeight * hour).fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "%02d:00".format(hour),
                        Modifier.width(60.dp).padding(start = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            placeEvents(events, state.selectedDate).forEach { placement ->
                val event = placement.event
                val width = eventWidth / placement.laneCount
                val height = (hourHeight * ((placement.endMinute - placement.startMinute) / 60f)).coerceAtLeast(48.dp)
                val owner = if (event.ownerId == state.user?.id) "Вы" else event.ownerName.substringBefore(" ")
                WhoBusyEventCard(
                    event.title,
                    "${eventTime(event)} · $owner · ${statusTitle(event.status)}",
                    calendarColor(event.displayColor),
                    { onEvent(CalendarCommand.OpenEvent(event)) },
                    Modifier
                        .offset(x = 64.dp + width * placement.lane, y = hourHeight * (placement.startMinute / 60f))
                        .width(width - 4.dp)
                        .height(height - 2.dp),
                )
            }
            if (now.toLocalDate() == state.selectedDate) {
                Row(
                    Modifier.offset(y = hourHeight * ((now.hour * 60 + now.minute) / 60f)).fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "%02d:%02d".format(now.hour, now.minute),
                        Modifier.width(60.dp).background(Color.White).padding(start = 4.dp),
                        color = WhoBusyColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalDivider(thickness = 2.dp, color = WhoBusyColors.Error)
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
