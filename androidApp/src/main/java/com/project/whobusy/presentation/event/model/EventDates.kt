package com.project.whobusy.presentation.event.model

import java.time.LocalDateTime
import java.time.LocalTime

internal fun EventFormState.withAllDay(value: Boolean): EventFormState {
    if (allDay == value) return this
    val first = LocalDateTime.parse(start)
    val finish = LocalDateTime.parse(end)
    val lastDate =
        if (value && finish.toLocalTime() == LocalTime.MIDNIGHT && finish > first) {
            finish.toLocalDate().minusDays(1)
        } else if (allDay) {
            finish.toLocalDate().minusDays(1)
        } else {
            finish.toLocalDate()
        }
    val last = maxOf(lastDate, first.toLocalDate())
    return copy(
        allDay = value,
        start = (if (value) first.toLocalDate().atStartOfDay() else first.toLocalDate().atTime(9, 0)).toString(),
        end = (if (value) last.plusDays(1).atStartOfDay() else last.atTime(10, 0)).toString(),
    )
}
