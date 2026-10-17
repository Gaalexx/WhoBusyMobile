package com.project.whobusy.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
    private val workspaceRepository: WorkspaceRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileState())
    val state = mutableState.asStateFlow()
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user -> mutableState.update { it.copy(user = user) } }
        }
    }

    fun onEvent(command: ProfileCommand) {
        when (command) {
            ProfileCommand.Refresh -> refreshInvitations()
            ProfileCommand.Logout -> authRepository.logout()
        }
    }

    private fun refreshInvitations() {
        refreshJob?.cancel()
        refreshJob =
            viewModelScope.launch {
                workspaceRepository
                    .getInvitations()
                    .onSuccess { invitations ->
                        mutableState.update { it.copy(invitationCount = invitations.size, error = null) }
                    }.onFailure {
                        mutableState.update { it.copy(error = R.string.auth_error_invitations_load) }
                    }
            }
    }
}
