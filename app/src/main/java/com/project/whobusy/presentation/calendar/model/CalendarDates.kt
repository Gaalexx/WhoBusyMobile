package com.project.whobusy.presentation.calendar.model

import com.project.whobusy.domain.model.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

val calendarLocale: Locale = Locale.forLanguageTag("ru")

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
