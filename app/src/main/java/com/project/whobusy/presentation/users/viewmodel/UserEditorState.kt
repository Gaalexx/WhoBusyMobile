package com.project.whobusy.presentation.users.viewmodel

import androidx.annotation.StringRes
import com.project.whobusy.domain.model.UserRole

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
    @param:StringRes val error: Int? = null,
) {
    val canDelete: Boolean get() = userId != null && userId != actingUserId && isAdmin
}
