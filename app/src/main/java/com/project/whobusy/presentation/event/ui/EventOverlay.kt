package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.presentation.event.model.EventFormState

@Composable
fun EventOverlay(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    if (!state.showEditor && state.selectedEvent == null) return
    Dialog(
        onDismissRequest = { onEvent(CalendarCommand.CloseEvent) },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = !state.isSaving,
                dismissOnClickOutside = false,
            ),
    ) {
        if (state.showEditor) {
            var form by rememberSaveable(
                state.selectedEvent?.id,
                state.selectedDate,
                stateSaver = EventFormState.Saver,
            ) {
                mutableStateOf(EventFormState.initial(state.selectedDate, state.selectedEvent))
            }
            EventEditorScreen(
                form,
                { if (!state.isSaving) form = it },
                {
                    val error = form.validate()
                    form = form.copy(validationError = error)
                    if (error == null) onEvent(CalendarCommand.SaveEvent(form.toDraft()))
                },
                {
                    onEvent(CalendarCommand.CloseEvent)
                },
                state.selectedEvent?.ownerName ?: state.user?.fullName.orEmpty(),
                editing = state.selectedEvent != null,
                saving = state.isSaving,
                error = state.error,
            )
        } else {
            state.selectedEvent?.let { event ->
                EventDetailsScreen(
                    event,
                    event.ownerId == state.user?.id || state.user?.role == UserRole.ADMIN,
                    { onEvent(CalendarCommand.CloseEvent) },
                    { onEvent(CalendarCommand.EditEvent) },
                    { onEvent(CalendarCommand.DeleteEvent) },
                    state.isSaving,
                    state.error,
                )
            }
        }
    }
}
