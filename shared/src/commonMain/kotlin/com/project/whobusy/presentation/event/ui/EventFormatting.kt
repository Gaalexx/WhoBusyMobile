package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.event.model.EventFormState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.plan_repeat_daily
import com.project.whobusy.resources.plan_repeat_monthly
import com.project.whobusy.resources.plan_repeat_never
import com.project.whobusy.resources.plan_repeat_other
import com.project.whobusy.resources.plan_repeat_weekdays
import com.project.whobusy.resources.plan_repeat_weekly
import com.project.whobusy.resources.plan_repeat_yearly
import com.project.whobusy.resources.plan_visibility_actors
import com.project.whobusy.resources.plan_visibility_busy
import com.project.whobusy.resources.plan_visibility_none
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.format
import com.project.whobusy.time.minusDays
import com.project.whobusy.time.parse
import com.project.whobusy.time.toLocalDate
import com.project.whobusy.time.value
import org.jetbrains.compose.resources.stringResource

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
            EventVisibility.Actors -> Res.string.plan_visibility_actors
            EventVisibility.Busy -> Res.string.plan_visibility_busy
            null -> Res.string.plan_visibility_none
        },
    )

internal val recurrenceOptions =
    listOf(
        null to Res.string.plan_repeat_never,
        "FREQ=DAILY" to Res.string.plan_repeat_daily,
        "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR" to Res.string.plan_repeat_weekdays,
        "FREQ=WEEKLY" to Res.string.plan_repeat_weekly,
        "FREQ=MONTHLY" to Res.string.plan_repeat_monthly,
        "FREQ=YEARLY" to Res.string.plan_repeat_yearly,
    )

@Composable
internal fun recurrenceTitle(rule: String?): String =
    stringResource(recurrenceOptions.firstOrNull { it.first == rule }?.second ?: Res.string.plan_repeat_other)
