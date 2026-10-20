package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState

@Composable
fun CalendarToolbar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    WhoBusyTopBar(
        stringResource(R.string.board_title),
        action = {
            WhoBusyIconButton(
                UiIcon.Filter,
                stringResource(R.string.board_settings),
                { onEvent(CalendarCommand.OpenCalendars) },
                tint = Color.White,
            )
        },
        subtitle =
            stringResource(
                R.string.board_subtitle,
                state.boardSettings.spaceIds.size,
                stringResource(
                    if (state.boardSettings.showOwn) R.string.board_with_own else R.string.board_without_own,
                ),
            ),
    )
}

@Preview
@Composable
private fun ToolbarPreview() {
    WhoBusyTheme { CalendarToolbar(CalendarPreviewData.state, {}) }
}
