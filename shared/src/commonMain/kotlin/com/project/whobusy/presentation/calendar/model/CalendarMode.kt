package com.project.whobusy.presentation.calendar.model

import com.project.whobusy.resources.Res
import com.project.whobusy.resources.calendar_mode_day
import com.project.whobusy.resources.calendar_mode_month
import com.project.whobusy.resources.calendar_mode_week
import org.jetbrains.compose.resources.StringResource

enum class CalendarMode(
    val titleRes: StringResource,
) {
    Day(Res.string.calendar_mode_day),
    Week(Res.string.calendar_mode_week),
    Month(Res.string.calendar_mode_month),
}
