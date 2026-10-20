package com.project.whobusy.presentation.calendar.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

internal enum class CalendarPagerPeriod {
    Day,
    Week,
    Month;

    val pageCount: Int
        get() = page(MAX_DATE) + 1

    fun page(date: LocalDate): Int = when (this) {
        Day -> ChronoUnit.DAYS.between(MIN_DATE, date)
        Week -> ChronoUnit.WEEKS.between(weekStart(MIN_DATE), weekStart(date))
        Month -> ChronoUnit.MONTHS.between(YearMonth.from(MIN_DATE), YearMonth.from(date))
    }.toInt()

    fun date(page: Int, preferredDate: LocalDate): LocalDate {
        require(page in 0 until pageCount)
        return when (this) {
            Day -> MIN_DATE.plusDays(page.toLong())
            Week -> weekStart(MIN_DATE).plusWeeks(page.toLong())
                .plusDays(preferredDate.dayOfWeek.value - 1L).coerceIn(MIN_DATE, MAX_DATE)

            Month -> YearMonth.from(MIN_DATE).plusMonths(page.toLong()).let { month ->
                month.atDay(preferredDate.dayOfMonth.coerceAtMost(month.lengthOfMonth()))
            }
        }
    }

    fun start(date: LocalDate): LocalDate = when (this) {
        Day -> date
        Week -> weekStart(date)
        Month -> date.withDayOfMonth(1)
    }

    companion object {
        private val MIN_DATE = LocalDate.of(1, 1, 1)
        private val MAX_DATE = LocalDate.of(9999, 12, 31)
    }
}
