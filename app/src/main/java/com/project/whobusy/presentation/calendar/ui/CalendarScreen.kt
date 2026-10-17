package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyAddButton
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import java.time.LocalDateTime

@Composable
fun CalendarScreen(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    now: LocalDateTime = LocalDateTime.now(),
) {
    val isEmptyDay = state.mode == CalendarMode.Day && eventsOnDate(state.events, state.selectedDate).isEmpty()
    val canCreateEvent = !state.isLoading && state.error == null && !isEmptyDay
    WhoBusyScreenLayout(topBar = { CalendarToolbar(state, onEvent) }) {
        Box(Modifier.fillMaxSize().testTag("calendar-content")) {
            Column(Modifier.fillMaxSize().background(Color.White)) {
                CalendarHeader(state, onEvent)
                when {
                    state.isLoading || state.error != null ->
                        CalendarLoadingError(state.error) {
                            onEvent(CalendarCommand.Refresh)
                        }

                    state.mode == CalendarMode.Month -> MonthCalendar(state, onEvent)
                    isEmptyDay ->
                        EmptyCalendar(
                            { onEvent(CalendarCommand.CreateEvent) },
                            { onEvent(CalendarCommand.OpenCalendars) },
                        )

                    else -> {
                        if (state.mode == CalendarMode.Day) {
                            CalendarLegend(state)
                            DayCalendar(state, onEvent, now)
                        } else {
                            WeekCalendar(state, onEvent)
                        }
                    }
                }
            }
            if (canCreateEvent) {
                WhoBusyAddButton(
                    onClick = { onEvent(CalendarCommand.CreateEvent) },
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(end = 16.dp, bottom = 90.dp),
                )
            }
        }
    }
}

@Preview(name = "День", widthDp = 390, heightDp = 844)
@Composable
private fun CalendarDayPreview() {
    WhoBusyTheme { CalendarScreen(CalendarPreviewData.state, {}, CalendarPreviewData.date.atTime(11, 45)) }
}

@Preview(name = "Неделя", widthDp = 390, heightDp = 844)
@Composable
private fun CalendarWeekPreview() {
    WhoBusyTheme { CalendarScreen(CalendarPreviewData.state.copy(mode = CalendarMode.Week), {}) }
}

@Preview(name = "Месяц", widthDp = 390, heightDp = 844)
@Composable
private fun CalendarMonthPreview() {
    WhoBusyTheme { CalendarScreen(CalendarPreviewData.state.copy(mode = CalendarMode.Month), {}) }
}

@Preview(name = "Нет событий", widthDp = 390, heightDp = 844)
@Composable
private fun CalendarEmptyPreview() {
    WhoBusyTheme { CalendarScreen(CalendarPreviewData.state.copy(events = emptyList()), {}) }
}
