package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel

@Composable
fun EventDetailsRoot(
    eventId: Int,
    onBack: () -> Unit,
    viewModel: CalendarViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val event = viewModel.event(eventId)
    if (event == null) {
        EventUnavailableScreen(state.isLoading, state.error, onBack) {
            viewModel.onEvent(CalendarCommand.Refresh)
        }
        return
    }
    EventDetailsScreen(
        event = event,
        canEdit = event.canEdit,
        onBack = onBack,
        onEdit = { viewModel.onEvent(CalendarCommand.EditEvent(eventId)) },
        onDelete = { viewModel.onEvent(CalendarCommand.DeleteEvent(eventId)) },
        saving = state.isSaving,
        error = state.error,
        spaces = state.spaces,
    )
}
