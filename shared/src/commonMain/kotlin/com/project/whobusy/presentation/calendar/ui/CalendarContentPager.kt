package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.CalendarPagerPeriod
import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.monthDates
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.YearMonth
import com.project.whobusy.time.atTime
import com.project.whobusy.time.from
import com.project.whobusy.time.now
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.plusWeeks
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun CalendarContentPager(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    now: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    val period = when (state.mode) {
        CalendarMode.Day -> CalendarPagerPeriod.Day
        CalendarMode.Week -> CalendarPagerPeriod.Week
        CalendarMode.Month -> CalendarPagerPeriod.Month
    }
    CalendarDatePager(
        period = period,
        selectedDate = state.selectedDate,
        onSelectDate = { onEvent(CalendarCommand.SelectDate(it)) },
        modifier = modifier.testTag("calendar-${state.mode.name.lowercase()}-pager"),
    ) { date ->
        val pageState = state.copy(selectedDate = date)
        Column(Modifier.fillMaxSize()) {
            if (state.mode == CalendarMode.Week) {
                CalendarWeekDays(date) { onEvent(CalendarCommand.SelectDate(it)) }
            }
            when {
                state.isLoading || state.error != null || !state.coversPage(date) -> {
                    CalendarLoadingError(state.error) { onEvent(CalendarCommand.Refresh) }
                }

                state.mode == CalendarMode.Month -> MonthCalendar(pageState, onEvent)
                state.mode == CalendarMode.Week -> WeekCalendar(pageState, onEvent)
                eventsOnDate(state.events, date).isEmpty() -> {
                    EmptyCalendar(
                        { onEvent(CalendarCommand.CreateEvent) },
                        { onEvent(CalendarCommand.OpenCalendars) },
                    )
                }

                else -> DayCalendar(pageState, onEvent, now)
            }
        }
    }
}

private fun CalendarState.coversPage(date: LocalDate): Boolean {
    // Standalone previews and UI fixtures can supply events directly without a loading window.
    val loadedStart = loadedStart ?: return true
    val loadedEnd = loadedEndExclusive ?: return true
    val start: LocalDate
    val end: LocalDate
    when (mode) {
        CalendarMode.Day -> {
            start = date
            end = date.plusDays(1)
        }

        CalendarMode.Week -> {
            start = weekStart(date)
            end = start.plusWeeks(1)
        }

        CalendarMode.Month -> {
            val dates = monthDates(YearMonth.from(date))
            start = dates.first()
            end = dates.last().plusDays(1)
        }
    }
    return start >= loadedStart && end <= loadedEnd
}

@Preview(name = "Страницы календаря", widthDp = 390, heightDp = 500)
@Composable
private fun CalendarContentPagerPreview() {
    WhoBusyTheme {
        CalendarContentPager(CalendarPreviewData.state, {}, CalendarPreviewData.date.atTime(11, 45))
    }
}

@Preview(name = "Загрузка соседней страницы", widthDp = 390, heightDp = 500)
@Composable
private fun CalendarContentPagerLoadingPreview() {
    WhoBusyTheme {
        CalendarContentPager(
            CalendarPreviewData.state.copy(isLoading = true), {}, CalendarPreviewData.date.atTime(11, 45),
        )
    }
}
