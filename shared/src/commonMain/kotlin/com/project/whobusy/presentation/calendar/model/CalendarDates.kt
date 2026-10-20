package com.project.whobusy.presentation.calendar.model

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.time.CalendarLocale
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.DayOfWeek
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.TemporalAdjusters
import com.project.whobusy.time.YearMonth
import com.project.whobusy.time.atDay
import com.project.whobusy.time.atEndOfMonth
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.format
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.titlecase
import com.project.whobusy.time.toEpochDay
import com.project.whobusy.time.with

val calendarLocale: CalendarLocale = CalendarLocale.Russian

fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun monthDates(month: YearMonth): List<LocalDate> {
    val start = weekStart(month.atDay(1))
    val last = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return List((last.toEpochDay() - start.toEpochDay() + 1).toInt()) { start.plusDays(it.toLong()) }
}

fun monthTitle(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("LLLL yyyy", calendarLocale)).replaceFirstChar {
        it.titlecase(calendarLocale)
    }

fun dayTitle(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", calendarLocale)).replaceFirstChar {
        it.titlecase(calendarLocale)
    }

fun eventsOnDate(
    events: List<CalendarEvent>,
    date: LocalDate,
): List<CalendarEvent> =
    events
        .filter { it.start < date.plusDays(1).atStartOfDay() && it.end > date.atStartOfDay() }
        .sortedWith(compareByDescending<CalendarEvent> { it.allDay }.thenBy { it.start })
