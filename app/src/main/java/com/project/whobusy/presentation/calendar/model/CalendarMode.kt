package com.project.whobusy.presentation.calendar.model

import androidx.annotation.StringRes
import com.project.whobusy.R

enum class CalendarMode(
    @param:StringRes val titleRes: Int,
) {
    Day(R.string.calendar_mode_day),
    Week(R.string.calendar_mode_week),
    Month(R.string.calendar_mode_month),
}
