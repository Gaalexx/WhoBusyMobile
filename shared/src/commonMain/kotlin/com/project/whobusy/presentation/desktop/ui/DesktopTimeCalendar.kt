package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.WhoBusyTimeGrid
import com.project.whobusy.core.ui.components.WhoBusyTimeGridItem
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.placeEvents
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.calendar_weekdays
import com.project.whobusy.resources.label_all_day
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atTime
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.toLocalDate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopTimeCalendar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    now: LocalDateTime,
) {
    val start = if (state.mode == CalendarMode.Day) state.selectedDate else weekStart(state.selectedDate)
    val dates = remember(start, state.mode) { List(if (state.mode == CalendarMode.Day) 1 else 7) { start.plusDays(it.toLong()) } }
    val placements = remember(dates, state.events) { dates.map { placeEvents(state.events, it) } }
    val density = LocalDensity.current
    Column(Modifier.fillMaxSize().background(Color.White)) {
        DesktopCalendarDayHeadings(dates, state.selectedDate, onEvent)
        DesktopAllDayRow(dates, state, onEvent)
        HorizontalDivider(color = DesktopPalette.Border)
        WhoBusyTimeGrid(
            hourHeight = 64.dp,
            timeColumnWidth = 64.dp,
            initialHour = 8,
            resetKey = start to state.mode,
            hourLabel = { "${it.toString().padStart(2, '0')}:00" },
            modifier = Modifier.weight(1f).testTag("desktop-time-grid"),
        ) { viewport ->
            val dayWidth = (maxWidth - 64.dp) / dates.size
            dates.forEachIndexed { index, date ->
                Box(
                    Modifier.offset(x = 64.dp + dayWidth * index).width(dayWidth).fillMaxHeight()
                        .background(if (date == state.selectedDate) DesktopPalette.Selection.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { onEvent(CalendarCommand.SelectDate(date)) },
                ) {
                    Box(Modifier.fillMaxHeight().width(1.dp).background(DesktopPalette.Border))
                }
                placements[index].filter { viewport.isVisible(it.startMinute, it.endMinute) }.forEach { placement ->
                    val width = dayWidth / placement.laneCount
                    key(date, placement.event.id, placement.event.start) {
                        WhoBusyTimeGridItem(
                            viewport = viewport,
                            startMinute = placement.startMinute,
                            endMinute = placement.endMinute,
                            left = 64.dp + dayWidth * index + width * placement.lane + 4.dp,
                            width = (width - 8.dp).coerceAtLeast(1.dp),
                        ) {
                            val height = with(density) {
                                viewport.eventHeight(placement.startMinute, placement.endMinute).toDp()
                            }
                            DesktopCalendarEventCard(
                                placement.event, { onEvent(CalendarCommand.OpenEvent(placement.event)) },
                                Modifier.fillMaxSize().testTag("desktop-event-${placement.event.id}"),
                                showTime = height >= 54.dp,
                                showText = height >= 32.dp,
                            )
                        }
                    }
                }
            }
            val todayIndex = dates.indexOf(now.toLocalDate())
            val minute = now.hour * 60 + now.minute
            if (todayIndex >= 0 && viewport.isVisible(minute, minute + 1)) {
                val top = with(density) { viewport.minuteY(minute).toDp() }
                Box(
                    Modifier.offset(x = 64.dp + dayWidth * todayIndex, y = top)
                        .width(dayWidth).height(2.dp).background(DesktopPalette.Accent),
                )
            }
        }
    }
}

@Composable
private fun DesktopCalendarDayHeadings(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onEvent: (CalendarCommand) -> Unit,
) {
    val weekdays = stringArrayResource(Res.array.calendar_weekdays)
    Row(Modifier.fillMaxWidth().height(72.dp)) {
        Box(Modifier.width(64.dp))
        dates.forEach { date ->
            Column(
                Modifier.weight(1f).fillMaxHeight().background(
                    if (date == selectedDate) DesktopPalette.Selection.copy(alpha = 0.3f) else Color.White,
                ).clickable { onEvent(CalendarCommand.SelectDate(date)) }.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(weekdays[date.dayOfWeek.ordinal].uppercase(), color = DesktopPalette.Muted, fontSize = 12.sp)
                Box(
                    Modifier.padding(top = 5.dp).size(32.dp).background(
                        if (date == selectedDate) DesktopPalette.Accent else Color.Transparent, CircleShape,
                    ), contentAlignment = Alignment.Center,
                ) {
                    Text(date.day.toString(), color = if (date == selectedDate) Color.White else DesktopPalette.Text, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun DesktopAllDayRow(dates: List<LocalDate>, state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp)) {
        Text(
            stringResource(Res.string.label_all_day), Modifier.width(64.dp).padding(10.dp),
            color = DesktopPalette.Muted, fontSize = 11.sp, textAlign = TextAlign.Start,
        )
        dates.forEach { date ->
            val allDay = remember(date, state.events) { eventsOnDate(state.events, date).filter { it.allDay } }
            LazyColumn(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 4.dp)) {
                items(allDay, key = { "${it.id}:${it.start}" }) { event ->
                    DesktopCalendarEventCard(
                        event, { onEvent(CalendarCommand.OpenEvent(event)) },
                        Modifier.fillMaxWidth().height(36.dp), showTime = false,
                    )
                }
            }
        }
    }
}

@Preview(name = "Неделя", widthDp = 792, heightDp = 808)
@Composable
private fun DesktopWeekPreview() {
    WhoBusyTheme {
        DesktopTimeCalendar(CalendarPreviewData.state.copy(mode = CalendarMode.Week), {}, CalendarPreviewData.date.atTime(10, 47))
    }
}

@Preview(name = "День", widthDp = 792, heightDp = 808)
@Composable
private fun DesktopDayPreview() {
    WhoBusyTheme { DesktopTimeCalendar(CalendarPreviewData.state, {}, CalendarPreviewData.date.atTime(10, 47)) }
}
