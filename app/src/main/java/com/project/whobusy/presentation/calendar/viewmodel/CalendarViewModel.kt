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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var loadJob: Job? = null
    private var pendingLoad: PendingLoad? = null
    private var loadVersion = 0L
    private val openedEvents = mutableMapOf<Pair<Int, String>, CalendarEvent>()

    fun rememberEvent(event: CalendarEvent) {
        openedEvents[event.id to event.start.toString()] = event
    }

    fun event(id: Int, occurrenceStart: String? = null): CalendarEvent? {
        val cached = if (occurrenceStart == null) {
            openedEvents.values.lastOrNull { it.id == id }
        } else {
            openedEvents[id to occurrenceStart]
        }
        return cached ?: _state.value.events.firstOrNull {
            it.id == id && (occurrenceStart == null || it.start.toString() == occurrenceStart)
        }
    }

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
                    when (_state.value.mode) {
                        CalendarMode.Day -> _state.value.selectedDate.plusDays(command.offset)
                        CalendarMode.Week -> _state.value.selectedDate.plusWeeks(command.offset)
                        CalendarMode.Month -> _state.value.selectedDate.plusMonths(command.offset)
                    },
                )

            is CalendarCommand.ToggleOwner -> toggleOwner(command.id)
            is CalendarCommand.OpenEvent -> rememberEvent(command.event)

            is CalendarCommand.SaveEvent ->
                mutate(CalendarEffect.EventSaved(command.eventId)) {
                    repository.saveEvent(command.draft, command.eventId).getOrThrow()
                }

            is CalendarCommand.DeleteEvent ->
                mutate(CalendarEffect.EventDeleted(command.eventId)) {
                    repository.deleteEvent(command.eventId).getOrThrow()
                }

            CalendarCommand.Today -> selectDate(LocalDate.now(clock))
            CalendarCommand.Refresh -> reload()
            CalendarCommand.CreateEvent, CalendarCommand.OpenCalendars -> Unit
            CalendarCommand.ResultHandled -> _state.update { it.copy(result = null) }
            CalendarCommand.ClearError -> _state.update { it.copy(error = null) }
            CalendarCommand.Logout -> viewModelScope.launch { auth.logout() }
        }
    }

    private fun selectDate(date: LocalDate) {
        savedState["date"] = date.toString()
        _state.update { it.copy(selectedDate = date) }
        if (_state.value.isSaving || pendingLoad?.range?.coversMonth(date) == true) return
        val current = _state.value
        val loadedRange = current.loadedStart?.let { start ->
            current.loadedEndExclusive?.let { end -> CalendarLoadRange(start, end) }
        }
        if (loadedRange?.coversMonth(date) == true && pendingLoad?.force != true) {
            cancelLoad()
            _state.update { it.copy(isLoading = false, error = null) }
            return
        }
        reload(force = pendingLoad?.force == true)
    }

    private fun toggleOwner(id: Int) {
        if (_state.value.isSaving || id !in _state.value.accessibleOwnerIds) return
        val selected = _state.value.visibleOwnerIds
        mutate {
            repository.setVisibleOwnerIds(if (id in selected) selected - id else selected + id).getOrThrow()
        }
    }

    private fun mutate(
        result: CalendarEffect? = null,
        block: suspend () -> Unit,
    ) {
        if (_state.value.isSaving || _state.value.result != null) return
        cancelLoad()
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                _state.update { it.copy(isSaving = false, result = result) }
                reload()
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

    private fun reload(force: Boolean = true) {
        if (_state.value.isSaving) return
        openedEvents.clear()
        cancelLoad()
        val date = _state.value.selectedDate
        val version = loadVersion
        pendingLoad = PendingLoad(calendarLoadRange(date), force)
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob =
            viewModelScope.launch {
                try {
                    val loaded = loader.load(date)
                    if (version != loadVersion) return@launch
                    _state.update {
                        loaded.copy(
                            selectedDate = it.selectedDate,
                            mode = it.mode,
                            isSaving = it.isSaving,
                            result = it.result,
                        )
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    if (version != loadVersion) return@launch
                    _state.update {
                        it.copy(isLoading = false, error = R.string.error_calendar_load)
                    }
                } finally {
                    if (version == loadVersion) {
                        pendingLoad = null
                        loadJob = null
                    }
                }
            }
    }

    private fun cancelLoad() {
        loadVersion++
        loadJob?.cancel()
        loadJob = null
        pendingLoad = null
    }

    private data class PendingLoad(
        val range: CalendarLoadRange,
        val force: Boolean,
    )
}
