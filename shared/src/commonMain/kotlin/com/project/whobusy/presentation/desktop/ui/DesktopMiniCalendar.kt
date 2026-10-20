package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.monthDates
import com.project.whobusy.presentation.calendar.model.monthTitle
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.calendar_weekdays
import com.project.whobusy.resources.desktop_next_month
import com.project.whobusy.resources.desktop_previous_month
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.YearMonth
import com.project.whobusy.time.atDay
import com.project.whobusy.time.from
import com.project.whobusy.time.plusMonths
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopMiniCalendar(selectedDate: LocalDate, onSelectDate: (LocalDate) -> Unit) {
    var month by remember(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(monthTitle(month.atDay(1)), Modifier.weight(1f), fontSize = 14.sp, color = DesktopPalette.Text)
            IconButton({ month = month.plusMonths(-1) }, Modifier.size(30.dp)) {
                WhoBusyIcon(UiIcon.Previous, stringResource(Res.string.desktop_previous_month))
            }
            IconButton({ month = month.plusMonths(1) }, Modifier.size(30.dp)) {
                WhoBusyIcon(UiIcon.Next, stringResource(Res.string.desktop_next_month))
            }
        }
        Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
            stringArrayResource(Res.array.calendar_weekdays).forEach { weekday ->
                Text(
                    weekday.uppercase(), Modifier.weight(1f), fontSize = 11.sp,
                    color = DesktopPalette.Muted, textAlign = TextAlign.Center,
                )
            }
        }
        monthDates(month).chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { date ->
                    val selected = date == selectedDate
                    Box(
                        Modifier.weight(1f).height(34.dp).clickable { onSelectDate(date) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier.size(28.dp).background(
                                if (selected) DesktopPalette.Accent else Color.Transparent, CircleShape,
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                date.day.toString(), fontSize = 12.sp,
                                color = when {
                                    selected -> Color.White
                                    YearMonth.from(date) != month -> DesktopPalette.Muted
                                    else -> DesktopPalette.Text
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 240, heightDp = 280)
@Composable
private fun DesktopMiniCalendarPreview() {
    WhoBusyTheme { DesktopMiniCalendar(CalendarPreviewData.date, {}) }
}
