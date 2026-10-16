package com.project.whobusy.presentation.calendar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.CalendarTab
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
                    tab = savedState.get<String>("tab")?.let(CalendarTab::valueOf) ?: CalendarTab.Calendar,
                ),
            )
        val state = _state.asStateFlow()
        private var loadJob: Job? = null

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
                is CalendarCommand.SelectTab -> {
                    savedState["tab"] = command.tab.name
                    _state.update { it.copy(tab = command.tab) }
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
                is CalendarCommand.OpenEvent -> _state.update { it.copy(selectedEvent = command.event) }
                is CalendarCommand.SaveEvent ->
                    mutate {
                        repository.saveEvent(command.draft, _state.value.selectedEvent?.id).getOrThrow()
                    }
                CalendarCommand.Today -> selectDate(LocalDate.now(clock))
                CalendarCommand.Refresh -> reload()
                CalendarCommand.CreateEvent ->
                    _state.update {
                        it.copy(showEditor = true, selectedEvent = null, error = null)
                    }
                CalendarCommand.EditEvent -> _state.update { it.copy(showEditor = true, error = null) }
                CalendarCommand.DeleteEvent ->
                    _state.value.selectedEvent?.let { event ->
                        mutate { repository.deleteEvent(event.id).getOrThrow() }
                    }
                CalendarCommand.CloseEvent ->
                    if (!_state.value.isSaving) {
                        _state.update { it.copy(selectedEvent = null, showEditor = false, error = null) }
                    }
                CalendarCommand.Logout -> auth.logout()
            }
        }

        private fun selectDate(date: LocalDate) {
            savedState["date"] = date.toString()
            _state.update { it.copy(selectedDate = date) }
            reload()
        }

        private fun toggleOwner(id: Int) {
            if (_state.value.isSaving || id !in _state.value.accessibleOwnerIds) return
            val selected = _state.value.visibleOwnerIds
            mutate(closeEvent = false) {
                repository.setVisibleOwnerIds(if (id in selected) selected - id else selected + id).getOrThrow()
            }
        }

        private fun mutate(
            closeEvent: Boolean = true,
            block: suspend () -> Unit,
        ) {
            if (_state.value.isSaving) return
            loadJob?.cancel()
            _state.update { it.copy(isSaving = true, error = null) }
            viewModelScope.launch {
                try {
                    block()
                    _state.update {
                        it.copy(
                            isSaving = false,
                            selectedEvent = if (closeEvent) null else it.selectedEvent,
                            showEditor = if (closeEvent) false else it.showEditor,
                        )
                    }
                    reload()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isLoading = false,
                            error = "Не удалось сохранить изменения. Попробуйте ещё раз.",
                        )
                    }
                }
            }
        }

        private fun reload() {
            loadJob?.cancel()
            val date = _state.value.selectedDate
            loadJob =
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, error = null) }
                    try {
                        val loaded = loader.load(date)
                        _state.update {
                            loaded.copy(
                                mode = it.mode,
                                tab = it.tab,
                                selectedEvent = it.selectedEvent,
                                showEditor = it.showEditor,
                            )
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        _state.update {
                            it.copy(isLoading = false, error = "Не удалось загрузить календарь. Проверьте подключение.")
                        }
                    }
                }
        }
    }
