package com.project.whobusy.presentation.calendar.model

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.time.ChronoUnit
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.DayOfWeek
import com.project.whobusy.time.Duration
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atTime
import com.project.whobusy.time.between
import com.project.whobusy.time.minus
import com.project.whobusy.time.parse
import com.project.whobusy.time.plus
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.toLocalDate
import com.project.whobusy.time.toLocalTime
import com.project.whobusy.time.withDayOfMonth

/** Expands the local, floating-time recurrence subset supported by the plan editor. */
fun expandRecurringEvents(
    events: List<CalendarEvent>,
    start: LocalDateTime,
    end: LocalDateTime,
    checkCancellation: () -> Unit = {},
): List<CalendarEvent> =
    events
        .flatMap { event ->
            checkCancellation()
            val rule =
                event.recurrenceRule
                    ?.removePrefix("RRULE:")
                    ?.split(";")
                    ?.mapNotNull { part ->
                        part.split("=", limit = 2).takeIf { it.size == 2 }?.let { it[0] to it[1] }
                    }?.toMap()
            if (rule == null || rule["FREQ"] !in setOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY")) {
                return@flatMap listOf(event).filter { it.end > start && it.start < end }
            }
            val interval = rule["INTERVAL"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val count = rule["COUNT"]?.toIntOrNull()?.coerceAtLeast(0) ?: Int.MAX_VALUE
            val until = rule["UNTIL"]?.let(::recurrenceUntil)
            val days = rule["BYDAY"]?.split(",")?.mapNotNull { recurrenceWeekdays[it] }?.toSet()
            val seriesStart = event.seriesStart ?: event.start
            val seriesEnd = event.seriesEnd ?: event.end
            val first = seriesStart.toLocalDate()
            val duration = Duration.between(seriesStart, seriesEnd)
            val result = mutableListOf<CalendarEvent>()
            var date = if (rule["COUNT"] == null) maxOf(first, start.minus(duration).toLocalDate()) else first
            var occurrence = 0
            var iterations = 0
            while (date <= end.toLocalDate() && occurrence < count) {
                if (iterations++ % 256 == 0) checkCancellation()
                val matches =
                    when (rule["FREQ"]) {
                        "DAILY" -> ChronoUnit.DAYS.between(first, date) % interval == 0L
                        "WEEKLY" ->
                            ChronoUnit.WEEKS.between(weekStart(first), weekStart(date)) % interval == 0L &&
                                date.dayOfWeek in (days ?: setOf(first.dayOfWeek))

                        "MONTHLY" ->
                            ChronoUnit.MONTHS.between(
                                first.withDayOfMonth(1),
                                date.withDayOfMonth(1),
                            ) % interval == 0L &&
                                date.dayOfMonth == first.dayOfMonth

                        "YEARLY" ->
                            (date.year - first.year) % interval == 0 &&
                                date.month == first.month && date.dayOfMonth == first.dayOfMonth

                        else -> false
                    }
                if (matches) {
                    val occurrenceStart = date.atTime(seriesStart.toLocalTime())
                    if (until != null && occurrenceStart > until) break
                    occurrence++
                    val occurrenceEnd = occurrenceStart.plus(duration)
                    if (occurrenceEnd > start && occurrenceStart < end) {
                        result +=
                            event.copy(
                                start = occurrenceStart,
                                end = occurrenceEnd,
                                seriesStart = seriesStart,
                                seriesEnd = seriesEnd,
                            )
                    }
                }
                date = date.plusDays(1)
            }
            result
        }.sortedBy { it.start }

private fun recurrenceUntil(value: String): LocalDateTime? =
    runCatching {
        if (value.length == 8) {
            LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE).atTime(23, 59, 59)
        } else {
            LocalDateTime.parse(value.removeSuffix("Z"), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"))
        }
    }.getOrNull()

private val recurrenceWeekdays =
    mapOf(
        "MO" to DayOfWeek.MONDAY,
        "TU" to DayOfWeek.TUESDAY,
        "WE" to DayOfWeek.WEDNESDAY,
        "TH" to DayOfWeek.THURSDAY,
        "FR" to DayOfWeek.FRIDAY,
        "SA" to DayOfWeek.SATURDAY,
        "SU" to DayOfWeek.SUNDAY,
    )
