package com.project.whobusy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.ui.DayCalendar
import com.project.whobusy.presentation.calendar.ui.WeekCalendar
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarTimelineTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun adjacentDayEventsMeetAtTheHourLine() {
        val first = event(101, 9, 0, 10, 0)
        val second = event(102, 10, 0, 11, 0)
        showCalendar {
            DayCalendar(state(first, second), {}, CalendarPreviewData.date.minusDays(1).atStartOfDay())
        }
        revealHourBoundary("day-time-grid")

        val firstBounds = bounds("day-event-${first.id}")
        val secondBounds = bounds("day-event-${second.id}")
        assertPixelEquals("First event starts at 09:00", hourLine(9), firstBounds.top)
        assertPixelEquals("First event ends at 10:00", hourLine(10), firstBounds.bottom)
        assertPixelEquals("Second event starts at 10:00", hourLine(10), secondBounds.top)
        assertPixelEquals("Second event ends at 11:00", hourLine(11), secondBounds.bottom)
        assertPixelEquals("Adjacent events share a boundary", firstBounds.bottom, secondBounds.top)
    }

    @Test
    fun fifteenMinuteDayEventKeepsItsActualDuration() {
        val short = event(103, 9, 15, 9, 30)
        showCalendar {
            DayCalendar(state(short), {}, CalendarPreviewData.date.minusDays(1).atStartOfDay())
        }
        revealHourBoundary("day-time-grid")

        val hourStart = hourLine(9)
        val hourHeight = hourLine(10) - hourStart
        val eventBounds = bounds("day-event-${short.id}")
        assertPixelEquals("Event starts at 09:15", hourStart + hourHeight / 4, eventBounds.top)
        assertPixelEquals("Event ends at 09:30", hourStart + hourHeight / 2, eventBounds.bottom)
        assertPixelEquals("Fifteen minutes occupy a quarter of an hour", hourHeight / 4, eventBounds.height)
    }

    @Test
    fun weekEventUsesTheSameHourBoundariesAsTheGrid() {
        val appointment = event(104, 9, 0, 10, 0)
        showCalendar { WeekCalendar(state(appointment), {}) }
        revealHourBoundary("week-time-grid")

        val eventBounds = bounds("week-event-${appointment.id}")
        assertPixelEquals("Week event starts at 09:00", hourLine(9), eventBounds.top)
        assertPixelEquals("Week event ends at 10:00", hourLine(10), eventBounds.bottom)
    }

    @Test
    fun longEventRemainsInteractiveWhileVisibleAndIsRemovedOutsideViewport() {
        val long = event(105, 9, 0, 16, 0)
        val distant = event(106, 1, 0, 2, 0)
        val opened = mutableListOf<Int>()
        showCalendar {
            // A bounded viewport can scroll beyond 16:00 even on a tall emulator.
            Column(Modifier.fillMaxWidth().height(360.dp)) {
                DayCalendar(
                    state(long, distant),
                    { command ->
                        if (command is CalendarCommand.OpenEvent) opened += command.event.id
                    },
                    CalendarPreviewData.date.minusDays(1).atStartOfDay(),
                )
            }
        }
        // The early event selects 01:00. Move past the long event's start, to 10:15.
        val hourHeight = hourLine(3) - hourLine(2)
        scrollBy("day-time-grid", hourHeight * 9.25f)
        composeRule.onNodeWithTag("day-event-${distant.id}").assertDoesNotExist()

        assertTrue(
            "The long event starts above the viewport",
            hourLine(11) - hourHeight * 2 < bounds("day-time-grid").top
        )
        val eventTag = "day-event-${long.id}"
        composeRule.onNodeWithTag(eventTag).assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf(long.id), opened) }
        val lineBeforeSwipe = hourLine(14)

        composeRule.onNodeWithTag(eventTag).performTouchInput {
            swipeUp(startY = height * 0.75f, endY = height * 0.5f, durationMillis = 1_500)
        }
        composeRule.waitForIdle()
        assertTrue("Dragging the event scrolls the timeline", hourLine(14) < lineBeforeSwipe - 1f)
        composeRule.onNodeWithTag(eventTag).assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf(long.id, long.id), opened) }

        scrollBy("day-time-grid", hourHeight * 8)
        composeRule.onNodeWithTag(eventTag).assertDoesNotExist()
    }

    @Test
    fun dayScrollPositionSurvivesSavedStateRestoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            WhoBusyTheme {
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxWidth().height(360.dp)) {
                        DayCalendar(
                            CalendarPreviewData.state,
                            {},
                            CalendarPreviewData.date.minusDays(1).atStartOfDay(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val hourHeight = hourLine(11) - hourLine(10)
        scrollBy("day-time-grid", hourHeight * 3.25f)
        val positionBeforeRestore = hourLine(14)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertPixelEquals(
            "The scrolled hour retains its position after restoration",
            positionBeforeRestore,
            hourLine(14)
        )
    }

    @Test
    fun eventEndingAtMidnightMeetsTheFinalHourLine() {
        val midnight = event(107, 23, 15, 23, 59).copy(
            end = CalendarPreviewData.date.plusDays(1).atStartOfDay(),
        )
        showCalendar {
            Column(Modifier.fillMaxWidth().height(360.dp)) {
                DayCalendar(state(midnight), {}, CalendarPreviewData.date.minusDays(1).atStartOfDay())
            }
        }
        composeRule.onNodeWithTag("day-time-grid").performSemanticsAction(SemanticsActions.ScrollToIndex) { action ->
            action(24)
        }
        composeRule.waitForIdle()

        val eventBounds = bounds("day-event-${midnight.id}")
        val hourHeight = hourLine(24) - hourLine(23)
        assertPixelEquals("Event starts at 23:15", hourLine(23) + hourHeight / 4, eventBounds.top)
        assertPixelEquals("Event ends at 24:00 before the footer", hourLine(24), eventBounds.bottom)
        assertPixelEquals("The final event lasts exactly 45 minutes", hourHeight * 0.75f, eventBounds.height)
    }

    private fun showCalendar(content: @Composable () -> Unit) {
        composeRule.setContent {
            WhoBusyTheme {
                Column(Modifier.fillMaxSize()) { content() }
            }
        }
        composeRule.waitForIdle()
    }

    private fun revealHourBoundary(gridTag: String) {
        // Keep both event edges inside the viewport, so bounds are not clipped at its top.
        scrollBy(gridTag, -with(composeRule.density) { 32.dp.toPx() })
    }

    private fun scrollBy(gridTag: String, pixels: Float) {
        composeRule.onNodeWithTag(gridTag).performSemanticsAction(SemanticsActions.ScrollBy) { action ->
            action(0f, pixels)
        }
        composeRule.waitForIdle()
    }

    private fun bounds(tag: String): Rect =
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

    private fun hourLine(hour: Int): Float = bounds("time-grid-line-$hour").center.y

    private fun assertPixelEquals(message: String, expected: Float, actual: Float) {
        assertEquals(message, expected, actual, 1f)
    }

    private fun state(vararg events: CalendarEvent): CalendarState = CalendarPreviewData.state.copy(
        events = events.toList(),
    )

    private fun event(id: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): CalendarEvent =
        CalendarPreviewData.events.first().copy(
            id = id,
            title = "Timeline event $id",
            start = CalendarPreviewData.date.atTime(startHour, startMinute),
            end = CalendarPreviewData.date.atTime(endHour, endMinute),
        )
}
