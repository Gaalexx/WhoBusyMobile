package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyDateCell
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarPagerPeriod
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.calendar_weekdays
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.plusDays
import org.jetbrains.compose.resources.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun CalendarWeekStripPager(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    CalendarDatePager(
        period = CalendarPagerPeriod.Week,
        selectedDate = selectedDate,
        onSelectDate = onSelectDate,
        modifier = Modifier.fillMaxWidth().testTag("calendar-week-strip-pager"),
    ) { date -> CalendarWeekDays(date, onSelectDate) }
}

@Composable
internal fun CalendarWeekDays(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 4.dp)) {
        val start = weekStart(selectedDate)
        stringArrayResource(Res.array.calendar_weekdays).forEachIndexed { index, label ->
            val date = start.plusDays(index.toLong())
            WhoBusyDateCell(
                date.day.toString(),
                dayTitle(date),
                date == selectedDate,
                { onSelectDate(date) },
                Modifier.weight(1f),
                weekday = label,
            )
        }
    }
}

@Preview(name = "Полоса недель", widthDp = 390)
@Composable
private fun CalendarWeekStripPagerPreview() {
    WhoBusyTheme { CalendarWeekStripPager(CalendarPreviewData.date, {}) }
}

@Preview(name = "Даты недели", widthDp = 390)
@Composable
private fun CalendarWeekDaysPreview() {
    WhoBusyTheme { CalendarWeekDays(CalendarPreviewData.date, {}) }
}
