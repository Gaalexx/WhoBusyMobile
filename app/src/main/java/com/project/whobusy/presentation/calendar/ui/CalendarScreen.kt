package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiDestination
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAddButton
import com.project.whobusy.core.ui.components.WhoBusyBottomBar
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.CalendarTab
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.presentation.calendars.ui.CalendarsScreen
import com.project.whobusy.presentation.profile.ui.ProfileScreen
import java.time.LocalDateTime

@Composable
fun CalendarScreen(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    now: LocalDateTime = LocalDateTime.now(),
) {
    val isEmptyDay = state.mode == CalendarMode.Day && eventsOnDate(state.events, state.selectedDate).isEmpty()
    val canCreateEvent = state.tab == CalendarTab.Calendar && !state.isLoading && state.error == null && !isEmptyDay

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .background(WhoBusyColors.Header)
                .windowInsetsTopHeight(WindowInsets.statusBars),
        )
        Scaffold(
            modifier = Modifier.weight(1f),
            contentWindowInsets = WindowInsets(0),
            topBar = { CalendarToolbar(state, onEvent) },
            bottomBar = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    WhoBusyBottomBar(
                        items =
                            listOf(
                                UiDestination("Календарь", UiIcon.Calendar),
                                UiDestination("Календари", UiIcon.People),
                                UiDestination("Профиль", UiIcon.Profile),
                            ),
                        selected = state.tab.ordinal,
                        onSelect = { onEvent(CalendarCommand.SelectTab(CalendarTab.entries[it])) },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    )
                }
            },
            floatingActionButton = {
                if (canCreateEvent) {
                    WhoBusyAddButton({ onEvent(CalendarCommand.CreateEvent) })
                }
            },
            containerColor = Color.White,
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                when (state.tab) {
                    CalendarTab.Calendars -> CalendarsScreen(state, onEvent)
                    CalendarTab.Profile ->
                        state.user?.let {
                            ProfileScreen(it) {
                                onEvent(CalendarCommand.Logout)
                            }
                        }

                    CalendarTab.Calendar -> {
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
                                    { onEvent(CalendarCommand.SelectTab(CalendarTab.Calendars)) },
                                )

                            else -> {
                                CalendarLegend(state)
                                if (state.mode == CalendarMode.Day) {
                                    DayCalendar(state, onEvent, now)
                                } else {
                                    WeekCalendar(state, onEvent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "День", widthDp = 390, heightDp = 844)
@Composable
private fun CalendarDayPreview() {
    WhoBusyTheme {
        CalendarScreen(
            CalendarPreviewData.state,
            {},
            CalendarPreviewData.date.atTime(11, 45),
        )
    }
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
