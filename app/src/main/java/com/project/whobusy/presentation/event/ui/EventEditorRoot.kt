package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.event.model.EventFormState
import java.time.LocalDate

@Composable
fun EventEditorRoot(
    date: String,
    eventId: Int?,
    onBack: () -> Unit,
    viewModel: CalendarViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val event = state.events.find { it.id == eventId }
    if (eventId != null && event == null) {
        EventUnavailableScreen(state.isLoading, state.error, onBack) {
            viewModel.onEvent(CalendarCommand.Refresh)
        }
        return
    }
    var form by rememberSaveable(stateSaver = EventFormState.Saver) {
        mutableStateOf(EventFormState.initial(LocalDate.parse(date), event))
    }
    EventEditorScreen(
        state = form,
        onChange = { if (!state.isSaving) form = it },
        onSave = {
            if (!state.isSaving) {
                val error = form.validate()
                form = form.copy(validationError = error)
                if (error == null) viewModel.onEvent(CalendarCommand.SaveEvent(form.toDraft(), eventId))
            }
        },
        onBack = onBack,
        owner = event?.ownerName ?: state.user?.fullName.orEmpty(),
        editing = eventId != null,
        saving = state.isSaving,
        error = state.error,
    )
}
