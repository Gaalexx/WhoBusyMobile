package com.project.whobusy.presentation.calendar.model

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

val calendarLocale: Locale = Locale.forLanguageTag("ru")
val weekLabels = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
fun monthDates(month: YearMonth): List<LocalDate> {
    val start = weekStart(month.atDay(1))
    val last = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return List((last.toEpochDay() - start.toEpochDay() + 1).toInt()) { start.plusDays(it.toLong()) }
}
fun monthTitle(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("LLLL yyyy", calendarLocale)).replaceFirstChar { it.titlecase(calendarLocale) }
fun dayTitle(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", calendarLocale)).replaceFirstChar { it.titlecase(calendarLocale) }
fun eventTime(event: CalendarEvent): String =
    if (event.allDay) "Весь день" else {
        val format = DateTimeFormatter.ofPattern("HH:mm")
        "${event.start.format(format)}–${event.end.format(format)}"
    }
fun statusTitle(status: EventStatus): String = when (status) {
    EventStatus.BUSY -> "Занят"
    EventStatus.FREE -> "Свободен"
    EventStatus.TENTATIVE -> "Под вопросом"
}
fun eventsOnDate(events: List<CalendarEvent>, date: LocalDate): List<CalendarEvent> =
    events.filter { it.start < date.plusDays(1).atStartOfDay() && it.end > date.atStartOfDay() }
        .sortedWith(compareByDescending<CalendarEvent> { it.allDay }.thenBy { it.start })
