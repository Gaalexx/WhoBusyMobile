package com.project.whobusy.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.R
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class ProfileViewModel(
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
        if (mutableState.value.isLoggingOut) return
        when (command) {
            ProfileCommand.Refresh -> refreshInvitations()
            ProfileCommand.Logout -> if (mutableState.value.user != null) logout(allDevices = false)
            ProfileCommand.RequestLogoutAll -> {
                if (mutableState.value.user != null) {
                    mutableState.update { it.copy(showLogoutAllConfirmation = true, error = null) }
                }
            }

            ProfileCommand.CancelLogoutAll -> mutableState.update { it.copy(showLogoutAllConfirmation = false) }
            ProfileCommand.ConfirmLogoutAll -> {
                if (mutableState.value.showLogoutAllConfirmation && mutableState.value.user != null) {
                    logout(allDevices = true)
                }
            }
        }
    }

    private fun logout(allDevices: Boolean) {
        refreshJob?.cancel()
        mutableState.update { it.copy(isLoggingOut = true, error = null) }
        viewModelScope.launch {
            try {
                if (allDevices) authRepository.logoutAll().getOrThrow() else authRepository.logout().getOrThrow()
                mutableState.update { it.copy(isLoggingOut = false, showLogoutAllConfirmation = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        isLoggingOut = false,
                        showLogoutAllConfirmation = false,
                        error = if (allDevices) R.string.auth_error_logout_all else R.string.auth_error_logout,
                    )
                }
            }
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
