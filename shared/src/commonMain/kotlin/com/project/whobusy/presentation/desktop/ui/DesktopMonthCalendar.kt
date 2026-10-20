package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.monthDates
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.ui.CalendarLegend
import com.project.whobusy.presentation.calendar.ui.eventTime
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.calendar_weekdays
import com.project.whobusy.resources.desktop_create_plan
import com.project.whobusy.resources.desktop_day_plans
import com.project.whobusy.resources.desktop_more_events
import com.project.whobusy.resources.desktop_no_day_plans
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.YearMonth
import com.project.whobusy.time.from
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopMonthCalendar(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    val month = YearMonth.from(state.selectedDate)
    val weeks = remember(month) { monthDates(month).chunked(7) }
    val dailyEvents = remember(state.events, weeks) {
        weeks.flatten().associateWith { eventsOnDate(state.events, it) }
    }
    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().height(62.dp), verticalAlignment = Alignment.CenterVertically) {
            stringArrayResource(Res.array.calendar_weekdays).forEach { weekday ->
                Text(
                    weekday.uppercase(), Modifier.weight(1f), color = DesktopPalette.Muted,
                    fontSize = 12.sp, textAlign = TextAlign.Center,
                )
            }
        }
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth().weight(1f)) {
                week.forEach { date ->
                    val events = dailyEvents.getValue(date)
                    Column(
                        Modifier.weight(1f).fillMaxHeight().border(0.5.dp, DesktopPalette.Border)
                            .background(if (date == state.selectedDate) DesktopPalette.Selection.copy(alpha = 0.3f) else Color.White)
                            .clickable { onEvent(CalendarCommand.SelectDate(date)) }.padding(4.dp),
                    ) {
                        MonthDayNumber(date, date == state.selectedDate, YearMonth.from(date) != month)
                        Spacer(Modifier.height(6.dp))
                        events.take(1).forEach { event ->
                            DesktopCalendarEventCard(
                                event, { onEvent(CalendarCommand.OpenEvent(event)) },
                                Modifier.fillMaxWidth().height(38.dp), showTime = false,
                            )
                        }
                        if (events.size > 1) {
                            Text(
                                stringResource(Res.string.desktop_more_events, events.size - 1),
                                Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                color = DesktopPalette.Muted, fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
        CalendarLegend(state)
    }
}

@Composable
private fun MonthDayNumber(date: LocalDate, selected: Boolean, muted: Boolean) {
    Box(
        Modifier.size(30.dp).background(if (selected) DesktopPalette.Accent else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            date.day.toString(), fontSize = 14.sp,
            color = when {
                selected -> Color.White
                muted -> DesktopPalette.Muted
                else -> DesktopPalette.Text
            },
        )
    }
}

@Composable
internal fun DesktopDayAgenda(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    val events = remember(state.events, state.selectedDate) { eventsOnDate(state.events, state.selectedDate) }
    Column(Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
        Text(
            stringResource(Res.string.desktop_day_plans, dayTitle(state.selectedDate).lowercase()),
            color = DesktopPalette.Text, fontSize = 20.sp, lineHeight = 28.sp,
        )
        Spacer(Modifier.height(24.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (events.isEmpty()) {
                item { Text(stringResource(Res.string.desktop_no_day_plans), color = DesktopPalette.Muted, fontSize = 14.sp) }
            }
            items(events, key = { "${it.id}:${it.start}" }) { event ->
                DesktopCalendarEventCard(
                    event, { onEvent(CalendarCommand.OpenEvent(event)) }, Modifier.fillMaxWidth(),
                    subtitle = "${eventTime(event)} · ${event.ownerName}",
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        WhoBusyButton(stringResource(Res.string.desktop_create_plan), { onEvent(CalendarCommand.CreateEvent) })
    }
}

@Preview(widthDp = 792, heightDp = 808)
@Composable
private fun DesktopMonthPreview() {
    WhoBusyTheme { DesktopMonthCalendar(CalendarPreviewData.state.copy(mode = CalendarMode.Month), {}) }
}

@Preview(name = "Планы дня", widthDp = 288, heightDp = 808)
@Composable
private fun DesktopDayAgendaPreview() {
    WhoBusyTheme { DesktopDayAgenda(CalendarPreviewData.state, {}) }
}

@Preview(name = "Пустой день", widthDp = 288, heightDp = 808)
@Composable
private fun DesktopEmptyAgendaPreview() {
    WhoBusyTheme { DesktopDayAgenda(CalendarPreviewData.state.copy(events = emptyList()), {}) }
}
