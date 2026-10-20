package com.project.whobusy.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours

class CalendarTimeTest {
    @Test
    fun floatingDateArithmeticKeepsLocalTimeAcrossDaylightSavingTransitions() {
        val start = LocalDateTime(2026, 3, 28, 2, 30)

        assertEquals(LocalDateTime(2026, 3, 29, 2, 30), start.plusDays(1))
        assertEquals(LocalDateTime(2026, 3, 29, 2, 30), start.plus(24.hours))
        assertEquals(24.hours, Duration.between(start, start.plusDays(1)))
    }

    @Test
    fun localDateTimeArithmeticPreservesSubMinutePrecision() {
        val start = LocalDateTime(2026, 12, 31, 23, 59, 45, 123_456_789)

        assertEquals(LocalDateTime(2027, 1, 1, 0, 0, 45, 123_456_789), start.plusMinutes(1))
        assertEquals(start, start.plusHours(2).minusHours(2))
    }

    @Test
    fun fixedClockUsesItsOwnTimeZone() {
        val clock = Clock.fixed(Instant.parse("2026-10-17T23:30:00Z"), TimeZone.of("Europe/Moscow"))

        assertEquals(LocalDate(2026, 10, 18), LocalDate.now(clock))
        assertEquals(LocalDateTime(2026, 10, 18, 2, 30), LocalDateTime.now(clock))
        assertEquals(1_792_279_800_000L, clock.millis())
    }

    @Test
    fun datePickerUtcConversionRoundTripsDatesAcrossTheSupportedRange() {
        listOf(LocalDate(1, 1, 1), LocalDate(2024, 2, 29), LocalDate(9999, 12, 31)).forEach { date ->
            assertEquals(date, LocalDate.fromEpochMillisecondsUtc(date.toEpochMillisecondsUtc()))
        }
    }

    @Test
    fun regionalTimeZoneUsesDaylightSavingRulesWhenReadingAnInstant() {
        val zone = TimeZone.of("Europe/Berlin")
        val before = Clock.fixed(Instant.parse("2026-03-29T00:30:00Z"), zone)
        val after = Clock.fixed(Instant.parse("2026-03-29T01:30:00Z"), zone)

        assertEquals(LocalDateTime(2026, 3, 29, 1, 30), LocalDateTime.now(before))
        assertEquals(LocalDateTime(2026, 3, 29, 3, 30), LocalDateTime.now(after))
    }

    @Test
    fun russianCalendarFormatsKeepMonthCaseAndLeadingZeros() {
        val date = LocalDateTime(2026, 10, 5, 9, 3)

        assertEquals("октябрь 2026", date.format(DateTimeFormatter.ofPattern("LLLL yyyy")))
        assertEquals("понедельник, 5 октября", date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))
        assertEquals("5 октября 2026, 09:03", date.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")))
        assertEquals("05.10", date.format(DateTimeFormatter.ofPattern("dd.MM")))
    }

    @Test
    fun compactRecurrenceFormatsRejectInvalidCalendarDates() {
        assertEquals(LocalDate(2024, 2, 29), LocalDate.parse("20240229", DateTimeFormatter.BASIC_ISO_DATE))
        assertFailsWith<IllegalArgumentException> {
            LocalDate.parse("20250229", DateTimeFormatter.BASIC_ISO_DATE)
        }
        assertEquals(
            LocalDateTime(2026, 10, 3, 9, 0),
            LocalDateTime.parse("20261003T090000", DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")),
        )
    }
}
