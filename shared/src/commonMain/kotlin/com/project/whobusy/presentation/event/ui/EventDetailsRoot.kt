package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarEffect
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.desktop.ui.DesktopPlanDetailsScreen

@Composable
fun EventDetailsRoot(
    eventId: Int,
    occurrenceStart: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: CalendarViewModel,
    desktopLayout: Boolean = false,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnDeleted by rememberUpdatedState(onDeleted)
    val canNavigate = {
        val current = viewModel.state.value
        !current.isSaving && current.result == null
    }
    val guardedOnBack = { if (canNavigate()) onBack() }
    LaunchedEffect(viewModel, state.result, eventId) {
        if (state.result == CalendarEffect.EventDeleted(eventId)) {
            viewModel.onEvent(CalendarCommand.ResultHandled)
            currentOnDeleted()
        }
    }
    val event = viewModel.event(eventId, occurrenceStart)
    if (event == null) {
        EventUnavailableScreen(state.isLoading, state.error, guardedOnBack) {
            viewModel.onEvent(CalendarCommand.Refresh)
        }
        return
    }
    if (desktopLayout) {
        DesktopPlanDetailsScreen(
            event = event,
            onBack = guardedOnBack,
            onEdit = { if (canNavigate()) onEdit() },
            onDelete = { viewModel.onEvent(CalendarCommand.DeleteEvent(eventId)) },
            saving = state.isSaving,
            error = state.error,
            spaces = state.spaces,
        )
    } else {
        EventDetailsScreen(
            event = event,
            canEdit = event.canEdit,
            onBack = guardedOnBack,
            onEdit = { if (canNavigate()) onEdit() },
            onDelete = { viewModel.onEvent(CalendarCommand.DeleteEvent(eventId)) },
            saving = state.isSaving,
            error = state.error,
            spaces = state.spaces,
        )
    }
}
