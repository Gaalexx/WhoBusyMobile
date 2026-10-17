package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.project.whobusy.R
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.event.model.EventFormState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun planDates(form: EventFormState): String {
    val start = LocalDateTime.parse(form.start)
    val end = LocalDateTime.parse(form.end)
    val date = start.format(DateTimeFormatter.ofPattern("d MMMM", calendarLocale))
    return if (form.allDay) {
        val last = end.toLocalDate().minusDays(1)
        if (last ==
            start.toLocalDate()
        ) {
            date
        } else {
            "$date — ${last.format(DateTimeFormatter.ofPattern("d MMMM", calendarLocale))}"
        }
    } else {
        val time = DateTimeFormatter.ofPattern("HH:mm", calendarLocale)
        if (start.toLocalDate() == end.toLocalDate()) {
            "$date, ${start.format(time)}–${end.format(time)}"
        } else {
            "${
                start.format(
                    DateTimeFormatter.ofPattern("d MMMM, HH:mm", calendarLocale),
                )
            } — ${end.format(DateTimeFormatter.ofPattern("d MMMM, HH:mm", calendarLocale))}"
        }
    }
}

@Composable
internal fun visibilityTitle(value: EventVisibility?): String =
    stringResource(
        when (value) {
            EventVisibility.Actors -> R.string.plan_visibility_actors
            EventVisibility.Busy -> R.string.plan_visibility_busy
            null -> R.string.plan_visibility_none
        },
    )

internal val recurrenceOptions =
    listOf(
        null to R.string.plan_repeat_never,
        "FREQ=DAILY" to R.string.plan_repeat_daily,
        "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR" to R.string.plan_repeat_weekdays,
        "FREQ=WEEKLY" to R.string.plan_repeat_weekly,
        "FREQ=MONTHLY" to R.string.plan_repeat_monthly,
        "FREQ=YEARLY" to R.string.plan_repeat_yearly,
    )

@Composable
internal fun recurrenceTitle(rule: String?): String =
    stringResource(recurrenceOptions.firstOrNull { it.first == rule }?.second ?: R.string.plan_repeat_other)
