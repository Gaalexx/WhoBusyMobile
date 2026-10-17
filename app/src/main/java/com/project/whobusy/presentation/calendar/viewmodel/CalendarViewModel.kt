package com.project.whobusy.presentation.calendar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.presentation.calendar.model.CalendarMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel
@Inject
constructor(
    private val repository: CalendarRepository,
    private val auth: AuthRepository,
    private val loader: CalendarLoader,
    private val clock: Clock,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val initialDate = savedState.get<String>("date")?.let(LocalDate::parse) ?: LocalDate.now(clock)
    private val _state =
        MutableStateFlow(
            CalendarState(
                initialDate,
                mode = savedState.get<String>("mode")?.let(CalendarMode::valueOf) ?: CalendarMode.Day,
            ),
        )
    val state = _state.asStateFlow()
    private val effectChannel = Channel<CalendarEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var loadJob: Job? = null
    private val openedEvents = mutableMapOf<Int, CalendarEvent>()

    fun event(id: Int): CalendarEvent? = openedEvents[id] ?: _state.value.events.firstOrNull { it.id == id }

    fun series(id: Int): CalendarEvent? =
        (_state.value.series.firstOrNull { it.id == id } ?: event(id))?.let {
            it.copy(start = it.seriesStart ?: it.start, end = it.seriesEnd ?: it.end)
        }

    init {
        reload()
    }

    fun onEvent(command: CalendarCommand) {
        when (command) {
            is CalendarCommand.SelectDate -> selectDate(command.date)
            is CalendarCommand.SelectMode -> {
                savedState["mode"] = command.mode.name
                _state.update { it.copy(mode = command.mode) }
            }

            is CalendarCommand.MovePeriod ->
                selectDate(
                    if (_state.value.mode == CalendarMode.Month) {
                        _state.value.selectedDate.plusMonths(command.offset)
                    } else {
                        _state.value.selectedDate.plusWeeks(command.offset)
                    },
                )

            is CalendarCommand.ToggleOwner -> toggleOwner(command.id)
            is CalendarCommand.OpenEvent -> {
                openedEvents[command.event.id] = command.event
                emitEffect(CalendarEffect.OpenEvent(command.event.id))
            }

            is CalendarCommand.SaveEvent ->
                mutate(CalendarEffect.EventSaved) {
                    repository.saveEvent(command.draft, command.eventId).getOrThrow()
                }

            is CalendarCommand.EditEvent -> emitEffect(CalendarEffect.EditEvent(command.eventId))
            is CalendarCommand.DeleteEvent ->
                mutate(CalendarEffect.EventDeleted) {
                    repository.deleteEvent(command.eventId).getOrThrow()
                }

            CalendarCommand.Today -> selectDate(LocalDate.now(clock))
            CalendarCommand.Refresh -> reload()
            CalendarCommand.CreateEvent ->
                emitEffect(CalendarEffect.CreateEvent(_state.value.selectedDate.toString()))

            CalendarCommand.OpenCalendars -> emitEffect(CalendarEffect.OpenCalendars)
            CalendarCommand.ClearError -> _state.update { it.copy(error = null) }
            CalendarCommand.Logout -> auth.logout()
        }
    }

    private fun emitEffect(effect: CalendarEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun selectDate(date: LocalDate) {
        savedState["date"] = date.toString()
        _state.update { it.copy(selectedDate = date) }
        reload()
    }

    private fun toggleOwner(id: Int) {
        if (_state.value.isSaving || id !in _state.value.accessibleOwnerIds) return
        val selected = _state.value.visibleOwnerIds
        mutate {
            repository.setVisibleOwnerIds(if (id in selected) selected - id else selected + id).getOrThrow()
        }
    }

    private fun mutate(
        successEffect: CalendarEffect? = null,
        block: suspend () -> Unit,
    ) {
        if (_state.value.isSaving) return
        loadJob?.cancel()
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                _state.update { it.copy(isSaving = false) }
                reload()
                if (successEffect != null) effectChannel.send(successEffect)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        isLoading = false,
                        error = R.string.error_calendar_save,
                    )
                }
            }
        }
    }

    private fun reload() {
        openedEvents.clear()
        loadJob?.cancel()
        val date = _state.value.selectedDate
        loadJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, error = null) }
                try {
                    val loaded = loader.load(date)
                    _state.update { loaded.copy(mode = it.mode) }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _state.update {
                        it.copy(isLoading = false, error = R.string.error_calendar_load)
                    }
                }
            }
    }
}
