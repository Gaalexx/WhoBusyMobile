package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.desktop.ui.DesktopCalendarScreen
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.now
import com.project.whobusy.time.value
import kotlinx.coroutines.delay

@Composable
fun CalendarRoot(
    viewModel: CalendarViewModel,
    onOpenEvent: (CalendarEvent) -> Unit,
    onCreateEvent: (String) -> Unit,
    onBoardSettings: () -> Unit,
    desktopLayout: Boolean = false,
    onOpenSpace: (Int) -> Unit = {},
    onSpaces: () -> Unit = {},
    onCreateSpace: () -> Unit = {},
    onJoinSpace: () -> Unit = {},
    detailContent: (@Composable () -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000)
        }
    }
    val canNavigate = {
        val current = viewModel.state.value
        !current.isSaving && current.result == null
    }
    val onEvent: (CalendarCommand) -> Unit = { command ->
        when (command) {
            is CalendarCommand.OpenEvent -> if (canNavigate()) onOpenEvent(command.event)
            CalendarCommand.CreateEvent -> if (canNavigate()) onCreateEvent(viewModel.state.value.selectedDate.toString())
            CalendarCommand.OpenCalendars -> if (canNavigate()) onBoardSettings()
            else -> viewModel.onEvent(command)
        }
    }
    if (desktopLayout) {
        DesktopCalendarScreen(
            state, onEvent, now,
            { if (canNavigate()) onOpenSpace(it) },
            { if (canNavigate()) onSpaces() },
            { if (canNavigate()) onCreateSpace() },
            { if (canNavigate()) onJoinSpace() },
            detailContent,
        )
    } else {
        CalendarScreen(state, onEvent, now)
    }
}
