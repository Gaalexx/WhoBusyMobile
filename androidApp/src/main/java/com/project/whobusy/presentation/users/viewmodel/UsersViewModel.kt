package com.project.whobusy.presentation.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class UsersViewModel(
    authRepository: AuthRepository,
    private val workspaceRepository: WorkspaceRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(UsersState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                val isAdmin = user?.role == UserRole.ADMIN
                mutableState.update { it.copy(isAdmin = isAdmin, users = if (isAdmin) it.users else emptyList()) }
            }
        }
    }

    fun onEvent(command: UsersCommand) {
        when (command) {
            UsersCommand.Refresh -> refresh()
        }
    }

    private fun refresh() {
        loadJob?.cancel()
        mutableState.update { it.copy(isLoading = true, error = null) }
        loadJob =
            viewModelScope.launch {
                workspaceRepository
                    .getUsers()
                    .onSuccess { users -> mutableState.update { it.copy(users = users, isLoading = false) } }
                    .onFailure {
                        mutableState.update { it.copy(isLoading = false, error = R.string.admin_error_load) }
                    }
            }
    }
}
