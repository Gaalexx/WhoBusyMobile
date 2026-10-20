package com.project.whobusy.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.auth_error_current_password
import com.project.whobusy.resources.auth_error_email_taken
import com.project.whobusy.resources.auth_error_last_name_length
import com.project.whobusy.resources.auth_error_name
import com.project.whobusy.resources.auth_error_password_length
import com.project.whobusy.resources.auth_error_profile_save
import com.project.whobusy.resources.error_login_email
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class ProfileEditorViewModel(
    authRepository: AuthRepository,
    private val workspaceRepository: WorkspaceRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileEditorState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null && state.value.userId == null) {
                    mutableState.value =
                        ProfileEditorState(
                            userId = user.id,
                            login = user.login,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email.orEmpty(),
                            color = user.color,
                        )
                }
            }
        }
    }

    fun onEvent(command: ProfileEditorCommand) {
        if (command == ProfileEditorCommand.ResultHandled) {
            mutableState.update { it.copy(isSaved = false) }
            return
        }
        if (state.value.isSaving || state.value.isSaved) return
        when (command) {
            is ProfileEditorCommand.FirstNameChanged ->
                mutableState.update { it.copy(firstName = command.value, firstNameError = null, error = null) }

            is ProfileEditorCommand.LastNameChanged ->
                mutableState.update { it.copy(lastName = command.value, lastNameError = null, error = null) }

            is ProfileEditorCommand.EmailChanged ->
                mutableState.update { it.copy(email = command.value, emailError = null, error = null) }

            is ProfileEditorCommand.ColorChanged ->
                mutableState.update { it.copy(color = command.value, error = null) }

            is ProfileEditorCommand.CurrentPasswordChanged ->
                mutableState.update {
                    it.copy(currentPassword = command.value, currentPasswordError = null, error = null)
                }

            is ProfileEditorCommand.NewPasswordChanged ->
                mutableState.update { it.copy(newPassword = command.value, passwordError = null, error = null) }

            ProfileEditorCommand.Save -> save()
            ProfileEditorCommand.ResultHandled -> Unit
        }
    }

    private fun save() {
        val current = state.value
        if (current.userId == null) return
        val checked =
            current.copy(
                firstNameError =
                    if (current.firstName.trim().length in 1..100) null else Res.string.auth_error_name,
                lastNameError =
                    if (current.lastName.trim().length <= 100) null else Res.string.auth_error_last_name_length,
                emailError =
                    if (current.email.isBlank() || EMAIL_PATTERN.matches(current.email.trim())) {
                        null
                    } else {
                        Res.string.error_login_email
                    },
                passwordError =
                    if (current.newPassword.isEmpty() || current.newPassword.length in 4..128) {
                        null
                    } else {
                        Res.string.auth_error_password_length
                    },
                currentPasswordError =
                    if (current.newPassword.isNotEmpty() && current.currentPassword.isEmpty()) {
                        Res.string.auth_error_current_password
                    } else {
                        null
                    },
                error = null,
            )
        mutableState.value = checked
        if (listOf(
                checked.firstNameError,
                checked.lastNameError,
                checked.emailError,
                checked.passwordError,
                checked.currentPasswordError,
            ).any { it != null }
        ) {
            return
        }
        mutableState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            workspaceRepository
                .updateProfile(
                    ProfileDraft(
                        firstName = current.firstName.trim(),
                        lastName = current.lastName.trim(),
                        email = current.email.trim().takeIf { it.isNotEmpty() },
                        color = current.color,
                        password = current.newPassword.takeIf { it.isNotEmpty() },
                        currentPassword = current.currentPassword.takeIf { it.isNotEmpty() },
                    ),
                ).onSuccess {
                    mutableState.update {
                        it.copy(isSaving = false, currentPassword = "", newPassword = "", isSaved = true)
                    }
                }.onFailure { error ->
                    mutableState.update {
                        when (error.message) {
                            "Current password is incorrect" ->
                                it.copy(
                                    isSaving = false,
                                    currentPasswordError = Res.string.auth_error_current_password,
                                )

                            "Email already registered" ->
                                it.copy(isSaving = false, emailError = Res.string.auth_error_email_taken)

                            else -> it.copy(isSaving = false, error = Res.string.auth_error_profile_save)
                        }
                    }
                }
        }
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
