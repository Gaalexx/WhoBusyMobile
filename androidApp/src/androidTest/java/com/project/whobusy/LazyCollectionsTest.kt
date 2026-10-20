package com.project.whobusy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.ui.CalendarLegend
import com.project.whobusy.presentation.calendar.ui.DayCalendar
import com.project.whobusy.presentation.calendar.ui.WeekCalendar
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.users.ui.UsersScreen
import com.project.whobusy.presentation.users.viewmodel.UsersState
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.atTime
import com.project.whobusy.time.minusDays
import com.project.whobusy.time.plusDays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LazyCollectionsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dayAllDayListKeepsTimelineVisibleAndOpensDistantEvent() {
        val allDay = allDayEvents()
        val timed = timedEvent()
        val opened = mutableListOf<Int>()
        showBoundedCalendar {
            DayCalendar(
                CalendarPreviewData.state.copy(events = allDay + timed),
                { command -> if (command is CalendarCommand.OpenEvent) opened += command.event.id },
                CalendarPreviewData.date.minusDays(1).atStartOfDay(),
            )
        }

        assertNonEmptyGrid("day-time-grid")
        composeRule.onNodeWithTag("day-event-${timed.id}").assertIsDisplayed()
        composeRule.onNodeWithText(allDay.last().title, substring = true).assertDoesNotExist()

        composeRule.onNodeWithTag("day-all-day-events").performScrollToIndex(allDay.lastIndex)
        composeRule.onNodeWithText(allDay.last().title, substring = true).assertIsDisplayed().performClick()

        assertNonEmptyGrid("day-time-grid")
        composeRule.onNodeWithTag("day-event-${timed.id}").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(listOf(allDay.last().id), opened) }
    }

    @Test
    fun weekAllDayListKeepsTimelineVisibleAndOpensDistantEvent() {
        val allDay = allDayEvents()
        val timed = timedEvent()
        val opened = mutableListOf<Int>()
        showBoundedCalendar {
            WeekCalendar(
                CalendarPreviewData.state.copy(events = allDay + timed),
                { command -> if (command is CalendarCommand.OpenEvent) opened += command.event.id },
            )
        }

        assertNonEmptyGrid("week-time-grid")
        composeRule.onNodeWithTag("week-event-${timed.id}").assertIsDisplayed()
        composeRule.onNodeWithText(allDay.last().title, substring = true).assertDoesNotExist()

        composeRule.onNodeWithTag("week-all-day-events").performScrollToIndex(allDay.lastIndex)
        composeRule.onNodeWithText(allDay.last().title, substring = true).assertIsDisplayed().performClick()

        assertNonEmptyGrid("week-time-grid")
        composeRule.onNodeWithTag("week-event-${timed.id}").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(listOf(allDay.last().id), opened) }
    }

    @Test
    fun calendarLegendComposesDistantOwnerOnlyAfterScrolling() {
        val users = users(100)
        val lastTag = "calendar-owner-${users.last().id}"
        composeRule.setContent {
            WhoBusyTheme {
                CalendarLegend(
                    CalendarPreviewData.state.copy(
                        user = users.first(),
                        users = users,
                        visibleOwnerIds = users.map { it.id }.toSet(),
                    ),
                )
            }
        }

        composeRule.onNodeWithTag(lastTag).assertDoesNotExist()

        composeRule.onNodeWithTag("calendar-legend").performScrollToIndex(users.lastIndex)

        composeRule.onNodeWithTag(lastTag).assertIsDisplayed()
    }

    @Test
    fun usersListScrollsToStableUserKeyAndOpensTheCorrectAccount() {
        val users = users(200)
        val opened = mutableListOf<Int?>()
        composeRule.setContent {
            WhoBusyTheme {
                UsersScreen(
                    UsersState(users = users, isLoading = false, isAdmin = true),
                    onBack = {},
                    onEdit = { opened += it },
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText(users.last().fullName).assertDoesNotExist()

        composeRule.onNodeWithTag("users-content").performScrollToKey(users.last().id)
        composeRule.onNodeWithText(users.last().fullName).assertIsDisplayed().performClick()

        composeRule.runOnIdle { assertEquals(listOf(users.last().id), opened) }
    }

    private fun showBoundedCalendar(content: @Composable () -> Unit) {
        composeRule.setContent {
            WhoBusyTheme {
                Box(Modifier.fillMaxWidth().height(500.dp)) { content() }
            }
        }
    }

    private fun assertNonEmptyGrid(tag: String) {
        val grid = composeRule.onNodeWithTag(tag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("The timeline must retain a nonzero viewport", grid.width > 0f && grid.height > 0f)
    }

    private fun allDayEvents(): List<CalendarEvent> = List(100) { index ->
        CalendarPreviewData.events.first().copy(
            id = 1_000 + index,
            title = "LazyAllDay${index.toString().padStart(3, '0')}",
            start = CalendarPreviewData.date.atStartOfDay(),
            end = CalendarPreviewData.date.plusDays(1).atStartOfDay(),
            allDay = true,
        )
    }

    private fun timedEvent(): CalendarEvent = CalendarPreviewData.events.first().copy(
        id = 2_000,
        title = "LazyTimedAppointment",
        start = CalendarPreviewData.date.atTime(9, 0),
        end = CalendarPreviewData.date.atTime(10, 0),
        allDay = false,
    )

    private fun users(count: Int): List<CalendarUser> = List(count) { index ->
        CalendarPreviewData.user.copy(
            id = 10_000 + index,
            fullName = "LazyOwner${index.toString().padStart(3, '0')} User",
            firstName = "LazyOwner${index.toString().padStart(3, '0')}",
            lastName = "User",
            login = "lazy_owner_$index",
            email = "lazy_owner_$index@example.test",
        )
    }
}
