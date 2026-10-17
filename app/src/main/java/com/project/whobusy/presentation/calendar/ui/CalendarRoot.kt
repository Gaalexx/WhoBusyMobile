package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun CalendarRoot(
    viewModel: CalendarViewModel,
    onOpenEvent: (CalendarEvent) -> Unit,
    onCreateEvent: (String) -> Unit,
    onBoardSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000)
        }
    }
    CalendarScreen(
        state = state,
        onEvent = { command ->
            when (command) {
                is CalendarCommand.OpenEvent -> onOpenEvent(command.event)
                CalendarCommand.CreateEvent -> onCreateEvent(state.selectedDate.toString())
                CalendarCommand.OpenCalendars -> onBoardSettings()
                else -> viewModel.onEvent(command)
            }
        },
        now = now,
    )
}
