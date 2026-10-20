package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.board_settings
import com.project.whobusy.resources.board_subtitle
import com.project.whobusy.resources.board_title
import com.project.whobusy.resources.board_with_own
import com.project.whobusy.resources.board_without_own
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CalendarToolbar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    WhoBusyTopBar(
        stringResource(Res.string.board_title),
        action = {
            WhoBusyIconButton(
                UiIcon.Filter,
                stringResource(Res.string.board_settings),
                { onEvent(CalendarCommand.OpenCalendars) },
                tint = Color.White,
            )
        },
        subtitle =
            stringResource(
                Res.string.board_subtitle,
                state.boardSettings.spaceIds.size,
                stringResource(
                    if (state.boardSettings.showOwn) Res.string.board_with_own else Res.string.board_without_own,
                ),
            ),
    )
}

@Preview
@Composable
private fun ToolbarPreview() {
    WhoBusyTheme { CalendarToolbar(CalendarPreviewData.state, {}) }
}
