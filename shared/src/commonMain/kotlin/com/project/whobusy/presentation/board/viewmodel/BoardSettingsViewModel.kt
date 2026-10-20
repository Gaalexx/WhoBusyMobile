package com.project.whobusy.presentation.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_error_action
import com.project.whobusy.resources.spaces_error_load
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class BoardSettingsViewModel(
    private val repository: WorkspaceRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(BoardSettingsState())
    val state = mutableState.asStateFlow()
    private var initialized = false

    fun onEvent(command: BoardSettingsCommand) {
        if (command == BoardSettingsCommand.ResultHandled) {
            mutableState.update { it.copy(isSaved = false) }
            return
        }
        val current = mutableState.value
        if (current.isLoading || current.isSaving || current.isSaved) return
        when (command) {
            BoardSettingsCommand.Load -> load()
            BoardSettingsCommand.Save -> save()
            BoardSettingsCommand.ResultHandled -> Unit
            BoardSettingsCommand.OnlyOwn -> mutableState.update { it.copy(settings = BoardSettings(emptySet())) }
            BoardSettingsCommand.AllSpaces ->
                mutableState.update {
                    it.copy(
                        settings =
                            BoardSettings(
                                it.spaces
                                    .map { space ->
                                        space.id
                                    }.toSet(),
                            ),
                    )
                }

            is BoardSettingsCommand.ShowOwn ->
                mutableState.update {
                    it.copy(
                        settings = it.settings.copy(showOwn = command.value),
                    )
                }

            is BoardSettingsCommand.ToggleSpace ->
                mutableState.update {
                    val selected = it.settings.spaceIds
                    it.copy(
                        settings =
                            it.settings.copy(
                                spaceIds =
                                    if (command.spaceId in
                                        selected
                                    ) {
                                        selected - command.spaceId
                                    } else {
                                        selected + command.spaceId
                                    },
                            ),
                    )
                }
        }
    }

    private fun load() {
        if (initialized) return
        mutableState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val spaces = repository.getSpaces().getOrThrow()
                val settings = repository.getBoardSettings().getOrThrow()
                mutableState.update { it.copy(spaces = spaces, settings = settings) }
                initialized = true
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableState.update { it.copy(error = Res.string.spaces_error_load) }
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun save() {
        mutableState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            repository
                .saveBoardSettings(mutableState.value.settings)
                .onSuccess { mutableState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure {
                    mutableState.update { it.copy(isSaving = false, error = Res.string.spaces_error_action) }
                }
        }
    }
}
