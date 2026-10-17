package com.project.whobusy.presentation.event.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.presentation.event.model.EventFormState
import com.project.whobusy.presentation.event.model.withAllDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EventEditorViewModel
@Inject
constructor(
    private val calendarRepository: CalendarRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val authRepository: AuthRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(EditorState(form = EventFormState.initial(LocalDate.now(clock), null)))
    val state = _state.asStateFlow()
    private val effectChannel = Channel<EditorEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var initializedKey: String? = null
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    fun start(
        date: String,
        event: CalendarEvent?,
        spaceId: Int?,
    ) {
        val key = "$date:${event?.id}:$spaceId"
        if (key == initializedKey) return
        initializedKey = key
        loadJob?.cancel()
        val restored =
            if (savedStateHandle.get<String>(ROUTE_KEY) == key) {
                savedStateHandle.get<ArrayList<String>>(DRAFT_KEY)?.let(EventFormState::restore)
            } else {
                null
            }
        val form =
            restored
                ?: EventFormState.initial(LocalDate.parse(date), event).copy(spaceId = event?.spaceId ?: spaceId)
        _state.value = EditorState(form = form, eventId = event?.id)
        loadJob =
            viewModelScope.launch {
                val spaces = workspaceRepository.getSpaces()
                val user = authRepository.currentUser.first()
                spaces.fold(
                    onSuccess = { list ->
                        val initial =
                            if (restored == null && event == null && form.spaceId == null) {
                                form.copy(shares = list.map { EventShare(it.id, EventVisibility.Busy) })
                            } else {
                                form
                            }
                        _state.update {
                            it.copy(
                                form = initial,
                                spaces = list,
                                canChangeVisibility = event == null || event.ownerId == user?.id,
                                isLoading = false,
                            )
                        }
                        persist()
                    },
                    onFailure = { failure ->
                        if (failure is AuthenticationException) authRepository.logout()
                        _state.update { it.copy(isLoading = false, error = R.string.plan_error_load) }
                    },
                )
            }
    }

    fun reset() {
        loadJob?.cancel()
        initializedKey = null
        savedStateHandle.remove<String>(ROUTE_KEY)
        savedStateHandle.remove<ArrayList<String>>(DRAFT_KEY)
        _state.value = EditorState(form = EventFormState.initial(LocalDate.now(clock), null))
    }

    fun onEvent(command: EditorCommand) {
        if (_state.value.isSaving || _state.value.isLoading) return
        if (command == EditorCommand.Save) {
            save()
            return
        }
        val current = _state.value
        val old = current.form
        val updated =
            when (command) {
                is EditorCommand.Title -> old.copy(title = command.value)
                is EditorCommand.Location -> old.copy(location = command.value)
                is EditorCommand.Description -> old.copy(description = command.value)
                is EditorCommand.Status -> old.copy(status = command.value)
                is EditorCommand.Recurrence -> old.copy(recurrenceRule = command.value)
                is EditorCommand.Placement -> {
                    if (current.eventId != null ||
                        (command.spaceId != null && current.spaces.none { it.id == command.spaceId })
                    ) {
                        return
                    }
                    old.copy(
                        spaceId = command.spaceId,
                        shares =
                            if (command.spaceId == null) {
                                current.spaces.map { EventShare(it.id, EventVisibility.Busy) }
                            } else {
                                emptyList()
                            },
                    )
                }

                is EditorCommand.AllDay -> {
                    if (old.allDay == command.value) return
                    old.withAllDay(command.value)
                }

                is EditorCommand.Start -> old.copy(start = command.value.toString())
                is EditorCommand.End -> old.copy(end = command.value.toString())
                is EditorCommand.Visibility -> {
                    if (!current.canChangeVisibility || old.spaceId != null ||
                        current.spaces.none { it.id == command.spaceId }
                    ) {
                        return
                    }
                    old.copy(
                        shares =
                            old.shares.filterNot { it.spaceId == command.spaceId } +
                                listOfNotNull(command.value?.let { EventShare(command.spaceId, it) }),
                    )
                }

                EditorCommand.Save -> old
            }
        _state.update { it.copy(form = updated.copy(validationError = null), error = null) }
        persist()
    }

    private fun persist() {
        savedStateHandle[ROUTE_KEY] = initializedKey
        savedStateHandle[DRAFT_KEY] = ArrayList(_state.value.form.snapshot())
    }

    private fun save() {
        val form = _state.value.form
        val error = form.validate()
        if (error != null) {
            _state.update { it.copy(form = form.copy(validationError = error)) }
            return
        }
        _state.update { it.copy(isSaving = true, error = null) }
        saveJob =
            viewModelScope.launch {
                calendarRepository.saveEvent(form.toDraft(), _state.value.eventId).fold(
                    onSuccess = {
                        _state.update { it.copy(isSaving = false) }
                        effectChannel.send(EditorEffect.Saved)
                    },
                    onFailure = { failure ->
                        if (failure is AuthenticationException) authRepository.logout()
                        _state.update { it.copy(isSaving = false, error = R.string.error_calendar_save) }
                    },
                )
            }
    }

    private companion object {
        const val ROUTE_KEY = "editor.route"
        const val DRAFT_KEY = "editor.draft"
    }
}
