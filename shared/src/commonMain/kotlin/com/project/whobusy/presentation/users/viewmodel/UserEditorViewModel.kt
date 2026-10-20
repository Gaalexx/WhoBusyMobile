package com.project.whobusy.presentation.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.admin_error_access
import com.project.whobusy.resources.admin_error_delete_self
import com.project.whobusy.resources.admin_error_last_admin
import com.project.whobusy.resources.admin_error_load
import com.project.whobusy.resources.admin_error_not_found
import com.project.whobusy.resources.admin_error_save
import com.project.whobusy.resources.auth_error_email_taken
import com.project.whobusy.resources.auth_error_last_name_length
import com.project.whobusy.resources.auth_error_login_format
import com.project.whobusy.resources.auth_error_login_taken
import com.project.whobusy.resources.auth_error_name
import com.project.whobusy.resources.auth_error_password_length
import com.project.whobusy.resources.error_login_email
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class UserEditorViewModel(
    authRepository: AuthRepository,
    private val workspaceRepository: WorkspaceRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(UserEditorState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                mutableState.update { it.copy(actingUserId = user?.id, isAdmin = user?.role == UserRole.ADMIN) }
            }
        }
    }

    fun onEvent(command: UserEditorCommand) {
        if (command == UserEditorCommand.ResultHandled) {
            mutableState.update { it.copy(isFinished = false) }
            return
        }
        if (state.value.isSaving || state.value.isFinished) return
        when (command) {
            is UserEditorCommand.Load -> load(command.userId)
            is UserEditorCommand.LoginChanged ->
                mutableState.update {
                    it.copy(
                        login = command.value,
                        error = null,
                    )
                }

            is UserEditorCommand.FirstNameChanged ->
                mutableState.update { it.copy(firstName = command.value, error = null) }

            is UserEditorCommand.LastNameChanged ->
                mutableState.update { it.copy(lastName = command.value, error = null) }

            is UserEditorCommand.EmailChanged ->
                mutableState.update {
                    it.copy(
                        email = command.value,
                        error = null,
                    )
                }

            is UserEditorCommand.PasswordChanged ->
                mutableState.update { it.copy(password = command.value, error = null) }

            is UserEditorCommand.RoleChanged -> mutableState.update { it.copy(role = command.value, error = null) }
            is UserEditorCommand.ColorChanged ->
                mutableState.update {
                    it.copy(
                        color = command.value,
                        error = null,
                    )
                }

            is UserEditorCommand.ActiveChanged ->
                mutableState.update {
                    it.copy(
                        isActive = command.value,
                        error = null,
                    )
                }

            UserEditorCommand.Save -> save()
            UserEditorCommand.RequestDelete ->
                mutableState.update { it.copy(showDeleteConfirmation = it.canDelete) }

            UserEditorCommand.CancelDelete -> mutableState.update { it.copy(showDeleteConfirmation = false) }
            UserEditorCommand.ConfirmDelete -> delete()
            UserEditorCommand.ResultHandled -> Unit
        }
    }

    private fun load(userId: Int?) {
        if (state.value.initialized && state.value.userId == userId) return
        loadJob?.cancel()
        mutableState.update {
            it.copy(
                userId = userId,
                initialized = true,
                isLoading = userId != null,
                error = null,
            )
        }
        if (userId == null) return
        loadJob =
            viewModelScope.launch {
                workspaceRepository
                    .getUsers()
                    .onSuccess { users ->
                        val user = users.firstOrNull { it.id == userId }
                        if (user == null) {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    error = Res.string.admin_error_not_found,
                                )
                            }
                        } else {
                            mutableState.update {
                                it.copy(
                                    isLoading = false,
                                    login = user.login,
                                    firstName = user.firstName,
                                    lastName = user.lastName,
                                    email = user.email.orEmpty(),
                                    role = user.role,
                                    color = user.color,
                                    isActive = user.isActive,
                                )
                            }
                        }
                    }.onFailure {
                        mutableState.update { it.copy(isLoading = false, error = Res.string.admin_error_load) }
                    }
            }
    }

    private fun save() {
        val current = state.value
        if (!current.initialized || current.isLoading) return
        val validation = validate(current)
        if (validation != null) {
            mutableState.update { it.copy(error = validation) }
            return
        }
        mutableState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            workspaceRepository
                .saveUser(
                    current.userId,
                    ManagedUserDraft(
                        login = current.login.trim(),
                        firstName = current.firstName.trim(),
                        lastName = current.lastName.trim(),
                        email = current.email.trim().takeIf { it.isNotEmpty() },
                        role = current.role,
                        color = current.color,
                        isActive = current.isActive,
                        password = current.password.takeIf { it.isNotEmpty() },
                    ),
                ).onSuccess {
                    mutableState.update { it.copy(isSaving = false, password = "", isFinished = true) }
                }.onFailure(::onFailure)
        }
    }

    private fun delete() {
        val current = state.value
        if (!current.canDelete || !current.showDeleteConfirmation) return
        mutableState.update { it.copy(isSaving = true, showDeleteConfirmation = false, error = null) }
        viewModelScope.launch {
            workspaceRepository
                .deleteUser(requireNotNull(current.userId))
                .onSuccess {
                    mutableState.update { it.copy(isSaving = false, isFinished = true) }
                }.onFailure(::onFailure)
        }
    }

    private fun onFailure(error: Throwable) {
        val message =
            when (error.message) {
                "Login already taken" -> Res.string.auth_error_login_taken
                "Email already registered" -> Res.string.auth_error_email_taken
                "The last administrator must remain active" -> Res.string.admin_error_last_admin
                "You cannot delete yourself" -> Res.string.admin_error_delete_self
                else -> Res.string.admin_error_save
            }
        mutableState.update { it.copy(isSaving = false, error = message) }
    }

    private fun validate(state: UserEditorState): StringResource? =
        when {
            !state.isAdmin -> Res.string.admin_error_access
            !LOGIN_PATTERN.matches(state.login.trim()) -> Res.string.auth_error_login_format
            state.firstName.trim().length !in 1..100 -> Res.string.auth_error_name
            state.lastName.trim().length > 100 -> Res.string.auth_error_last_name_length
            state.email.isNotBlank() && !EMAIL_PATTERN.matches(state.email.trim()) -> Res.string.error_login_email
            (state.userId == null || state.password.isNotEmpty()) && state.password.length !in 4..128 ->
                Res.string.auth_error_password_length

            else -> null
        }

    private companion object {
        val LOGIN_PATTERN = Regex("[a-zA-Z0-9._-]{3,32}")
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
