package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarTab
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState

@Composable
fun CalendarToolbar(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    WhoBusyTopBar(state.tab.title, action = {
        if (state.tab == CalendarTab.Calendar) Box {
            WhoBusyIconButton(
                UiIcon.More, "Меню календаря", { expanded = true }, tint = Color.White
            )
            DropdownMenu(expanded, { expanded = false }) {
                listOf(
                    "Предыдущий период" to CalendarCommand.MovePeriod(-1),
                    "Следующий период" to CalendarCommand.MovePeriod(1),
                    "Обновить" to CalendarCommand.Refresh
                ).forEach { (title, command) ->
                    DropdownMenuItem(
                        text = { Text(title) },
                        onClick = { expanded = false; onEvent(command) })
                }
            }
        }
    })
}

@Preview
@Composable
private fun ToolbarPreview() {
    WhoBusyTheme {
        CalendarToolbar(
            com.project.whobusy.presentation.calendar.preview.CalendarPreviewData.state, {})
    }
}
