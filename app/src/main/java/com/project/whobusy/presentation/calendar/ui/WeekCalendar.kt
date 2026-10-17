package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.whobusy.core.ui.components.WhoBusyTimeGrid
import com.project.whobusy.core.ui.components.WhoBusyTimeGridItem
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.placeEvents
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import java.time.format.DateTimeFormatter

@Composable
fun WeekCalendar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    val start = weekStart(state.selectedDate)
    val density = LocalDensity.current
    val dates = remember(start) { List(7) { start.plusDays(it.toLong()) } }
    val placements = remember(state.events, dates) { dates.map { placeEvents(state.events, it) } }
    val textLineHeight = with(density) { 12.sp.roundToPx() }
    val cardPadding = with(density) { 6.dp.roundToPx() }
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }
    Column(Modifier.fillMaxSize()) {
        WeekAllDayEvents(state, onEvent)
        WhoBusyTimeGrid(
            hourHeight = 56.dp,
            timeColumnWidth = 34.dp,
            initialHour = 9,
            resetKey = start,
            hourLabel = { "%02d".format(it) },
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("week-time-grid"),
        ) { viewport ->
            val dayWidth = (maxWidth - 34.dp) / 7
            dates.forEachIndexed { index, date ->
                Box(
                    Modifier
                        .offset(x = 34.dp + dayWidth * index)
                        .width(dayWidth)
                        .fillMaxHeight()
                        .clickable {
                            onEvent(CalendarCommand.SelectDate(date))
                            onEvent(CalendarCommand.SelectMode(CalendarMode.Day))
                        },
                ) {
                    Box(Modifier.fillMaxHeight().width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }
                placements[index].filter { viewport.isVisible(it.startMinute, it.endMinute) }.forEach { placement ->
                    val event = placement.event
                    val width = dayWidth / placement.laneCount
                    val availableTextHeight =
                        viewport.eventHeight(placement.startMinute, placement.endMinute) - cardPadding
                    key(date, event.id, event.start) {
                        WhoBusyTimeGridItem(
                            viewport = viewport,
                            startMinute = placement.startMinute,
                            endMinute = placement.endMinute,
                            left = 34.dp + dayWidth * index + width * placement.lane + 2.dp,
                            width = (width - 4.dp).coerceAtLeast(1.dp),
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(eventColor(event))
                                    .clickable { onEvent(CalendarCommand.OpenEvent(event)) }
                                    .testTag("week-event-${event.id}")
                                    .semantics {
                                        if (availableTextHeight < textLineHeight) contentDescription = event.title
                                    }
                                    .padding(3.dp),
                            ) {
                                if (availableTextHeight >= textLineHeight * 2) {
                                    Text(
                                        event.start.format(timeFormat),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        maxLines = 1,
                                    )
                                }
                                if (availableTextHeight >= textLineHeight) {
                                    Text(
                                        event.title,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        maxLines = if (availableTextHeight >= textLineHeight * 3) 2 else 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 450, showBackground = true)
@Composable
private fun WeekCalendarPreview() {
    WhoBusyTheme { WeekCalendar(CalendarPreviewData.state, {}) }
}

@Preview(name = "Короткие и многочасовые события", widthDp = 390, heightDp = 450, showBackground = true)
@Composable
private fun WeekCalendarDurationsPreview() {
    val date = CalendarPreviewData.date
    val event = CalendarPreviewData.events.first()
    val events = listOf(
        event.copy(id = 1, start = date.atTime(9, 0), end = date.atTime(9, 5)),
        event.copy(id = 2, start = date.atTime(9, 15), end = date.atTime(9, 45)),
        event.copy(id = 3, start = date.atTime(10, 0), end = date.atTime(11, 0)),
        event.copy(id = 4, start = date.atTime(12, 0), end = date.atTime(23, 0)),
    )
    WhoBusyTheme { WeekCalendar(CalendarPreviewData.state.copy(events = events), {}) }
}
