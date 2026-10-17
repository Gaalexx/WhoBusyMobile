package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyDateCell
import com.project.whobusy.core.ui.components.WhoBusyEventCard
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.monthDates
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import java.time.YearMonth

@Composable
fun MonthCalendar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    val month = YearMonth.from(state.selectedDate)
    val daily = eventsOnDate(state.events, state.selectedDate).sortedBy { it.allDay }
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding =
            PaddingValues(
                bottom =
                    90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
    ) {
        item {
            Column(Modifier.background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row {
                    stringArrayResource(R.array.calendar_weekdays).forEach {
                        Text(
                            it,
                            Modifier.weight(1f).padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                monthDates(month).chunked(7).forEach { week ->
                    Row {
                        week.forEach { date ->
                            WhoBusyDateCell(
                                date.dayOfMonth.toString(),
                                dayTitle(date),
                                date == state.selectedDate,
                                { onEvent(CalendarCommand.SelectDate(date)) },
                                Modifier.weight(1f),
                                muted = YearMonth.from(date) != month,
                                eventColors =
                                    eventsOnDate(state.events, date)
                                        .map {
                                            calendarColor(it.displayColor)
                                        }.distinct(),
                            )
                        }
                    }
                }
            }
        }
        item {
            Text(
                dayTitle(state.selectedDate),
                Modifier.padding(20.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        items(daily.take(2), key = { it.id }) { event ->
            val owner =
                if (event.ownerId ==
                    state.user?.id
                ) {
                    stringResource(R.string.label_you)
                } else {
                    event.ownerName.substringBefore(" ")
                }
            WhoBusyEventCard(
                event.title,
                stringResource(R.string.event_summary, eventTime(event), owner, statusTitle(event.status)),
                calendarColor(event.displayColor),
                { onEvent(CalendarCommand.OpenEvent(event)) },
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth()
                    .height(68.dp),
            )
        }
        if (daily.size > 2) {
            item {
                WhoBusyTextButton(
                    stringResource(R.string.calendar_more_events, daily.size - 2),
                    { onEvent(CalendarCommand.SelectMode(CalendarMode.Day)) },
                    Modifier.padding(horizontal = 8.dp),
                )
            }
        }
        if (daily.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.calendar_no_events_on_date),
                    Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 590)
@Composable
private fun MonthCalendarPreview() {
    WhoBusyTheme { MonthCalendar(CalendarPreviewData.state, {}) }
}
