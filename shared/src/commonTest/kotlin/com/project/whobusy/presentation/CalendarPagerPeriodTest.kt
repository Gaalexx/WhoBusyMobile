package com.project.whobusy.presentation

import com.project.whobusy.presentation.calendar.model.CalendarPagerPeriod
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.Test
import com.project.whobusy.time.DayOfWeek
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.of
import com.project.whobusy.time.plusDays

class CalendarPagerPeriodTest {
    @Test
    fun monthPagingPreservesPreferredDayAfterShortMonth() {
        val period = CalendarPagerPeriod.Month
        val preferred = LocalDate.of(2025, 1, 31)
        val january = period.page(preferred)

        assertEquals(LocalDate.of(2025, 2, 28), period.date(january + 1, preferred))
        assertEquals(LocalDate.of(2025, 3, 31), period.date(january + 2, preferred))
        assertEquals(preferred, period.date(january, preferred))
    }

    @Test
    fun pagingBackwardAcrossFebruaryAlsoPreservesPreferredDay() {
        val period = CalendarPagerPeriod.Month
        val preferred = LocalDate.of(2025, 3, 31)
        val march = period.page(preferred)

        assertEquals(LocalDate.of(2025, 2, 28), period.date(march - 1, preferred))
        assertEquals(LocalDate.of(2025, 1, 31), period.date(march - 2, preferred))
    }

    @Test
    fun monthClampUsesGregorianLeapYearRules() {
        val period = CalendarPagerPeriod.Month
        val preferred = LocalDate.of(2024, 1, 31)

        listOf(1900 to 28, 2000 to 29, 2024 to 29, 2025 to 28).forEach { (year, lastDay) ->
            val february = period.page(LocalDate.of(year, 2, 1))
            assertEquals(LocalDate.of(year, 2, lastDay), period.date(february, preferred))
        }
    }

    @Test
    fun weekPagingCrossesYearBoundaryWithoutChangingWeekday() {
        val period = CalendarPagerPeriod.Week
        val sunday = LocalDate.of(2023, 12, 31)
        val page = period.page(sunday)

        assertEquals(LocalDate.of(2024, 1, 7), period.date(page + 1, sunday))
        assertEquals(LocalDate.of(2023, 12, 24), period.date(page - 1, sunday))
        assertEquals(LocalDate.of(2023, 12, 25), period.start(sunday))
        assertEquals(DayOfWeek.MONDAY, period.start(sunday).dayOfWeek)
    }

    @Test
    fun datesInTheSameMondayWeekSharePageAcrossDifferentYears() {
        val period = CalendarPagerPeriod.Week
        val monday = LocalDate.of(2019, 12, 30)

        repeat(7) { day ->
            assertEquals(period.page(monday), period.page(monday.plusDays(day.toLong())))
        }
        assertEquals(period.page(monday) + 1, period.page(LocalDate.of(2020, 1, 6)))
    }

    @Test
    fun selectedDateRoundTripsThroughEveryPeriodIncludingRangeBoundaries() {
        val dates = listOf(
            LocalDate.of(1, 1, 1),
            LocalDate.of(99, 12, 31),
            LocalDate.of(1900, 2, 28),
            LocalDate.of(2000, 2, 29),
            LocalDate.of(2024, 12, 31),
            LocalDate.of(9999, 12, 31),
        )

        CalendarPagerPeriod.entries.forEach { period ->
            dates.forEach { date ->
                val page = period.page(date)
                assertTrue(page in 0 until period.pageCount, "$period: $date")
                assertEquals(date, period.date(page, date), "$period: $date")
            }
        }
    }

    @Test
    fun pageRangeIncludesFirstAndLastSupportedDates() {
        val first = LocalDate.of(1, 1, 1)
        val last = LocalDate.of(9999, 12, 31)

        CalendarPagerPeriod.entries.forEach { period ->
            assertEquals(0, period.page(first))
            assertEquals(period.pageCount - 1, period.page(last))
        }
        assertEquals(3_652_059, CalendarPagerPeriod.Day.pageCount)
        assertEquals(521_723, CalendarPagerPeriod.Week.pageCount)
        assertEquals(119_988, CalendarPagerPeriod.Month.pageCount)
    }

    @Test
    fun lastWeekClampsPreferredWeekendToSupportedDateRange() {
        val period = CalendarPagerPeriod.Week
        val preferredSunday = LocalDate.of(2025, 1, 5)

        assertEquals(LocalDate.of(9999, 12, 31), period.date(period.pageCount - 1, preferredSunday))
    }

    @Test
    fun pagesOutsideSupportedRangeAreRejected() {
        CalendarPagerPeriod.entries.forEach { period ->
            assertFailsWith<IllegalArgumentException> { period.date(-1, LocalDate.of(2025, 1, 1)) }
            assertFailsWith<IllegalArgumentException> {
                period.date(period.pageCount, LocalDate.of(2025, 1, 1))
            }
        }
    }
}
