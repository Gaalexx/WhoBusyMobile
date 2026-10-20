package com.project.whobusy.presentation.users.viewmodel

import com.project.whobusy.domain.model.UserRole
import org.jetbrains.compose.resources.StringResource

data class UserEditorState(
    val userId: Int? = null,
    val actingUserId: Int? = null,
    val isAdmin: Boolean = false,
    val initialized: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val login: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.USER,
    val color: String = "#2481CC",
    val isActive: Boolean = true,
    val showDeleteConfirmation: Boolean = false,
    val error: StringResource? = null,
    val isFinished: Boolean = false,
) {
    val canDelete: Boolean get() = userId != null && userId != actingUserId && isAdmin
}
