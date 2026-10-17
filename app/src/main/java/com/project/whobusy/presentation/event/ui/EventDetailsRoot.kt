package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel

@Composable
fun EventDetailsRoot(
    eventId: Int,
    onBack: () -> Unit,
    viewModel: CalendarViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val event = state.events.find { it.id == eventId }
    if (event == null) {
        EventUnavailableScreen(state.isLoading, state.error, onBack) {
            viewModel.onEvent(CalendarCommand.Refresh)
        }
        return
    }
    EventDetailsScreen(
        event = event,
        canEdit = event.ownerId == state.user?.id || state.user?.role == UserRole.ADMIN,
        onBack = onBack,
        onEdit = { viewModel.onEvent(CalendarCommand.EditEvent(eventId)) },
        onDelete = { viewModel.onEvent(CalendarCommand.DeleteEvent(eventId)) },
        saving = state.isSaving,
        error = state.error,
    )
}
