package com.project.whobusy.presentation

import com.project.whobusy.presentation.calendar.model.eventsOnDate
import com.project.whobusy.presentation.calendar.model.monthDates
import com.project.whobusy.presentation.calendar.model.placeEvents
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.event.model.EventFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarDatesTest {
    @Test
    fun monthGridIncludesLeapDayAndWholeMondayWeeks() {
        val dates = monthDates(YearMonth.of(2024, 2))
        assertEquals(DayOfWeek.MONDAY, dates.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, dates.last().dayOfWeek)
        assertTrue(LocalDate.of(2024, 2, 29) in dates)
        assertEquals(0, dates.size % 7)
    }

    @Test
    fun midnightEndDoesNotLeakIntoNextDay() {
        val date = CalendarPreviewData.date
        val event = CalendarPreviewData.events.last()
        assertEquals(listOf(event), eventsOnDate(listOf(event), date))
        assertTrue(eventsOnDate(listOf(event), date.plusDays(1)).isEmpty())
    }

    @Test
    fun overlappingEventsHaveSeparateLanesButTouchingEventsDoNot() {
        val date = CalendarPreviewData.date
        val first = CalendarPreviewData.events.first()
        val overlap = first.copy(id = 2, start = date.atTime(9, 30), end = date.atTime(10, 30))
        val following = first.copy(id = 3, start = date.atTime(10, 30), end = date.atTime(11, 30))
        val positions = placeEvents(listOf(first, overlap, following), date)
        assertEquals(listOf(2, 2, 1), positions.map { it.laneCount })
        assertEquals(listOf(0, 1, 0), positions.map { it.lane })
    }

    @Test
    fun multiDayEventIsClippedToEachDaysTimeBoundaries() {
        val date = CalendarPreviewData.date
        val event = CalendarPreviewData.events.first().copy(
            start = date.minusDays(1).atTime(23, 30),
            end = date.plusDays(1).atTime(0, 30),
        )

        val firstDay = placeEvents(listOf(event), date.minusDays(1)).single()
        val fullDay = placeEvents(listOf(event), date).single()
        val lastDay = placeEvents(listOf(event), date.plusDays(1)).single()

        assertEquals(1_410, firstDay.startMinute)
        assertEquals(1_440, firstDay.endMinute)
        assertEquals(0, fullDay.startMinute)
        assertEquals(1_440, fullDay.endMinute)
        assertEquals(0, lastDay.startMinute)
        assertEquals(30, lastDay.endMinute)
    }

    @Test
    fun eventEditorRejectsBlankTitleAndReversedTime() {
        val state = EventFormState.initial(CalendarPreviewData.date, null)
        assertNotNull(state.validate())
        assertNotNull(state.copy(title = "Встреча", end = state.start).validate())
        assertNull(state.copy(title = "Встреча").validate())
    }
}
