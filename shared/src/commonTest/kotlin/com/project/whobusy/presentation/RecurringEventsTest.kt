package com.project.whobusy.presentation

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.model.expandRecurringEvents
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.Test
import com.project.whobusy.time.Duration
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.between
import com.project.whobusy.time.ofDays
import com.project.whobusy.time.ofHours
import com.project.whobusy.time.plusHours
import com.project.whobusy.time.toLocalDate

class RecurringEventsTest {
    @Test
    fun dailyIntervalAndCountIncludeOccurrencesBeforeTheRequestedWindow() {
        val event = event("2026-10-01T09:00", "FREQ=DAILY;INTERVAL=2;COUNT=4")

        val result = expand(event, "2026-10-05", "2026-10-10")

        assertEquals(listOf("2026-10-05", "2026-10-07"), result.map { it.start.toLocalDate().toString() })
        assertTrue(result.all { Duration.between(it.start, it.end) == Duration.ofHours(1) })
        assertTrue(result.all { it.id == event.id })
    }

    @Test
    fun weeklyIntervalUsesMondayWeeksAndSelectedWeekdays() {
        val event = event("2026-10-05T09:00", "RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE;COUNT=5")

        val result = expand(event, "2026-10-01", "2026-11-03")

        assertEquals(
            listOf("2026-10-05", "2026-10-07", "2026-10-19", "2026-10-21", "2026-11-02"),
            result.map { it.start.toLocalDate().toString() },
        )
    }

    @Test
    fun monthlyRecurrenceSkipsMonthsWithoutTheStartingDay() {
        val event = event("2026-01-31T09:00", "FREQ=MONTHLY;COUNT=3")

        assertEquals(
            listOf("2026-01-31", "2026-03-31", "2026-05-31"),
            expand(event, "2026-01-01", "2026-07-01").map { it.start.toLocalDate().toString() },
        )
    }

    @Test
    fun leapDayYearlyRecurrenceSkipsNonLeapYears() {
        val event = event("2024-02-29T09:00", "FREQ=YEARLY;COUNT=3")

        assertEquals(
            listOf(2024, 2028, 2032),
            expand(event, "2024-01-01", "2033-01-01").map { it.start.year },
        )
    }

    @Test
    fun untilIncludesTheLastDateOrExactTimestamp() {
        val event = event("2026-10-01T09:00", "FREQ=DAILY;UNTIL=20261003")
        assertEquals(3, expand(event, "2026-10-01", "2026-10-06").size)
        assertEquals(
            3,
            expand(event.copy(recurrenceRule = "FREQ=DAILY;UNTIL=20261003T090000Z"), "2026-10-01", "2026-10-06").size,
        )
        assertEquals(
            2,
            expand(event.copy(recurrenceRule = "FREQ=DAILY;UNTIL=20261003T085959"), "2026-10-01", "2026-10-06").size,
        )
    }

    @Test
    fun multiDayOccurrenceOverlappingTheWindowIsIncludedAndRangeEndIsExclusive() {
        val event =
            event("2026-10-01T00:00", "FREQ=DAILY;INTERVAL=3;COUNT=2").copy(
                end = LocalDateTime.parse("2026-10-03T00:00"),
                allDay = true,
            )
        val result =
            expandRecurringEvents(
                listOf(event),
                LocalDateTime.parse("2026-10-02T12:00"),
                LocalDateTime.parse("2026-10-04T00:00"),
            )

        assertEquals(1, result.size)
        assertTrue(result.single().allDay)
        assertEquals(Duration.ofDays(2), Duration.between(result.single().start, result.single().end))
        assertFalse(result.any { it.start == LocalDateTime.parse("2026-10-04T00:00") })
    }

    @Test
    fun unknownRuleFallsBackToSingleOccurrenceAndZeroCountProducesNone() {
        val event = event("2026-10-01T09:00", "FREQ=HOURLY")

        assertEquals(listOf(event), expand(event, "2026-10-01", "2026-10-02"))
        assertTrue(expand(event, "2026-10-02", "2026-10-03").isEmpty())
        assertTrue(expand(event.copy(recurrenceRule = "FREQ=DAILY;COUNT=0"), "2026-10-01", "2026-10-03").isEmpty())
    }

    @Test
    fun ancientUnboundedSeriesOnlyScansTheRequestedWindowAndKeepsMasterTimes() {
        val event = event("1900-01-01T09:00", "FREQ=DAILY")
        var checks = 0
        val result =
            expandRecurringEvents(
                listOf(event),
                LocalDateTime.parse("2026-10-01T00:00"),
                LocalDateTime.parse("2026-10-08T00:00"),
                checkCancellation = {
                    check(++checks <= 3) { "An unbounded series scanned its complete history" }
                },
            )

        assertEquals(7, result.size)
        assertEquals(LocalDateTime.parse("2026-10-01T09:00"), result.first().start)
        assertTrue(result.all { it.seriesStart == event.start && it.seriesEnd == event.end })
    }

    @Test
    fun expansionChecksCancellationWhileCountingHistoricOccurrences() {
        val event = event("1900-01-01T09:00", "FREQ=DAILY;COUNT=100000")
        var checks = 0

        assertFailsWith<IllegalStateException> {
            expandRecurringEvents(
                listOf(event),
                LocalDateTime.parse("2026-10-01T00:00"),
                LocalDateTime.parse("2026-10-08T00:00"),
                checkCancellation = { if (++checks == 3) throw IllegalStateException("Cancelled") },
            )
        }
    }

    private fun expand(
        event: CalendarEvent,
        start: String,
        end: String,
    ) = expandRecurringEvents(
        listOf(event),
        LocalDate.parse(start).atStartOfDay(),
        LocalDate.parse(end).atStartOfDay(),
    )

    private fun event(
        start: String,
        rule: String,
    ) = CalendarEvent(
        id = 42,
        ownerId = 1,
        ownerName = "Автор",
        title = "Повторяющийся план",
        description = "Описание",
        location = "Онлайн",
        start = LocalDateTime.parse(start),
        end = LocalDateTime.parse(start).plusHours(1),
        allDay = false,
        status = EventStatus.BUSY,
        displayColor = "#2481CC",
        recurrenceRule = rule,
    )
}
