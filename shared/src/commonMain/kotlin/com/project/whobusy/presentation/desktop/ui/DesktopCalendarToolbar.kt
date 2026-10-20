package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.model.monthTitle
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_today
import com.project.whobusy.resources.board_settings
import com.project.whobusy.resources.desktop_create_plan
import com.project.whobusy.resources.desktop_next_period
import com.project.whobusy.resources.desktop_own_plans
import com.project.whobusy.resources.desktop_previous_period
import com.project.whobusy.resources.desktop_refresh
import com.project.whobusy.resources.desktop_shared_board
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.format
import com.project.whobusy.time.plusDays
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopCalendarToolbar(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().height(80.dp).background(DesktopPalette.Rail).padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(stringResource(Res.string.desktop_shared_board), color = Color.White, fontSize = 22.sp)
                val subtitle = state.spaces.filter { it.id in state.boardSettings.spaceIds }.map { it.name } +
                    if (state.boardSettings.showOwn) listOf(stringResource(Res.string.desktop_own_plans).lowercase()) else emptyList()
                Text(
                    subtitle.joinToString(" · "), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton({ onEvent(CalendarCommand.Refresh) }, enabled = !state.isLoading && !state.isSaving) {
                WhoBusyIcon(UiIcon.Refresh, stringResource(Res.string.desktop_refresh), tint = Color.White)
            }
            IconButton({ onEvent(CalendarCommand.OpenCalendars) }) {
                WhoBusyIcon(UiIcon.Filter, stringResource(Res.string.board_settings), tint = Color.White)
            }
            Button(
                { onEvent(CalendarCommand.CreateEvent) },
                Modifier.height(44.dp), enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DesktopPalette.Accent),
                shape = RoundedCornerShape(10.dp),
            ) {
                WhoBusyIcon(UiIcon.Add, tint = DesktopPalette.Accent)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.desktop_create_plan), fontSize = 14.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth().height(72.dp).background(Color.White).padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(desktopPeriodTitle(state), fontSize = 20.sp, color = DesktopPalette.Text)
            Spacer(Modifier.width(20.dp))
            IconButton({ onEvent(CalendarCommand.MovePeriod(-1)) }) {
                WhoBusyIcon(UiIcon.Previous, stringResource(Res.string.desktop_previous_period))
            }
            IconButton({ onEvent(CalendarCommand.MovePeriod(1)) }) {
                WhoBusyIcon(UiIcon.Next, stringResource(Res.string.desktop_next_period))
            }
            TextButton(
                { onEvent(CalendarCommand.Today) },
                Modifier.background(DesktopPalette.Selection, RoundedCornerShape(10.dp)),
            ) { Text(stringResource(Res.string.action_today)) }
            Spacer(Modifier.weight(1f))
            CalendarMode.entries.forEach { mode ->
                TextButton(
                    { onEvent(CalendarCommand.SelectMode(mode)) },
                    Modifier.width(84.dp).height(44.dp).background(
                        if (state.mode == mode) DesktopPalette.Selection else Color.Transparent, RoundedCornerShape(10.dp),
                    ),
                ) {
                    Text(
                        stringResource(mode.titleRes), fontSize = 14.sp,
                        color = if (state.mode == mode) DesktopPalette.Accent else DesktopPalette.Muted,
                    )
                }
            }
        }
        HorizontalDivider(color = DesktopPalette.Border)
    }
}

internal fun desktopPeriodTitle(state: CalendarState): String = when (state.mode) {
    CalendarMode.Day -> dayTitle(state.selectedDate)
    CalendarMode.Month -> monthTitle(state.selectedDate)
    CalendarMode.Week -> {
        val first = weekStart(state.selectedDate)
        val last = first.plusDays(6)
        if (first.month == last.month) {
            "${first.day}–${last.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}"
        } else {
            "${first.format(DateTimeFormatter.ofPattern("d MMMM"))} — ${last.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}"
        }
    }
}

@Preview(widthDp = 1080, heightDp = 153)
@Composable
private fun DesktopCalendarToolbarPreview() {
    WhoBusyTheme { DesktopCalendarToolbar(CalendarPreviewData.state.copy(mode = CalendarMode.Week), {}) }
}
