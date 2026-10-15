package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.*
import com.project.whobusy.core.ui.theme.*
import com.project.whobusy.presentation.calendar.model.*
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.*
import java.time.YearMonth

@Composable
fun MonthCalendar(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    val month = YearMonth.from(state.selectedDate)
    val daily = eventsOnDate(state.events, state.selectedDate).sortedBy { it.allDay }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 88.dp)) {
        item {
            Column(Modifier.background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row { weekLabels.forEach { Text(it, Modifier.weight(1f).padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                monthDates(month).chunked(7).forEach { week ->
                    Row {
                        week.forEach { date ->
                            WhoBusyDateCell(date.dayOfMonth.toString(), dayTitle(date), date == state.selectedDate,
                                { onEvent(CalendarCommand.SelectDate(date)) }, Modifier.weight(1f),
                                muted = YearMonth.from(date) != month,
                                eventColors = eventsOnDate(state.events, date).map { calendarColor(it.displayColor) }.distinct())
                        }
                    }
                }
            }
        }
        item { Text(dayTitle(state.selectedDate), Modifier.padding(20.dp),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        items(daily.take(2), key = { it.id }) { event ->
            val owner = if (event.ownerId == state.user?.id) "Вы" else event.ownerName.substringBefore(" ")
            WhoBusyEventCard(event.title, "${eventTime(event)} · $owner · ${statusTitle(event.status)}",
                calendarColor(event.displayColor), { onEvent(CalendarCommand.OpenEvent(event)) },
                Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp).fillMaxWidth().height(68.dp))
        }
        if (daily.size > 2) item {
            WhoBusyTextButton("Ещё событий: ${daily.size - 2}",
                { onEvent(CalendarCommand.SelectMode(CalendarMode.Day)) }, Modifier.padding(horizontal = 8.dp))
        }
        if (daily.isEmpty()) item {
            Text("На этот день событий нет", Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(widthDp = 390, heightDp = 590)
@Composable
private fun MonthCalendarPreview() { WhoBusyTheme { MonthCalendar(CalendarPreviewData.state, {}) } }
