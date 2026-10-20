package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.ui.CalendarLoadingError
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atTime

@Composable
internal fun DesktopCalendarScreen(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    now: LocalDateTime,
    onOpenSpace: (Int) -> Unit,
    onSpaces: () -> Unit,
    onCreateSpace: () -> Unit,
    onJoinSpace: () -> Unit,
    detailContent: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.White).testTag("desktop-calendar")) {
        val showSidebar = maxWidth >= 1080.dp
        Row(Modifier.fillMaxSize()) {
            if (showSidebar) {
                DesktopSpaceSidebar(state, onEvent, onOpenSpace, onSpaces, onCreateSpace, onJoinSpace)
                VerticalDivider(color = DesktopPalette.Border)
            }
            Column(Modifier.weight(1f)) {
                DesktopCalendarToolbar(state, onEvent)
                Row(Modifier.weight(1f)) {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        when {
                            state.isLoading || state.error != null -> {
                                CalendarLoadingError(state.error) { onEvent(CalendarCommand.Refresh) }
                            }
                            state.mode == CalendarMode.Month -> DesktopMonthCalendar(state, onEvent)
                            else -> DesktopTimeCalendar(state, onEvent, now)
                        }
                    }
                    if (detailContent != null || state.mode == CalendarMode.Month) {
                        VerticalDivider(color = DesktopPalette.Border)
                        Box(Modifier.width(288.dp).fillMaxHeight()) {
                            if (detailContent != null) detailContent() else DesktopDayAgenda(state, onEvent)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Доска Web", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopBoardPreview() {
    WhoBusyTheme {
        DesktopCalendarScreen(
            CalendarPreviewData.state.copy(mode = CalendarMode.Week), {}, CalendarPreviewData.date.atTime(10, 47),
            {}, {}, {}, {},
        )
    }
}

@Preview(name = "Месяц Web", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopBoardMonthPreview() {
    WhoBusyTheme {
        DesktopCalendarScreen(
            CalendarPreviewData.state.copy(mode = CalendarMode.Month), {}, CalendarPreviewData.date.atTime(10, 47),
            {}, {}, {}, {},
        )
    }
}

@Preview(name = "Ошибка доски", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopBoardLoadingPreview() {
    WhoBusyTheme {
        DesktopCalendarScreen(
            CalendarPreviewData.state.copy(isLoading = true), {}, CalendarPreviewData.date.atTime(10, 47),
            {}, {}, {}, {},
        )
    }
}
