package com.project.whobusy.presentation.profile.viewmodel

import androidx.annotation.StringRes

data class ProfileEditorState(
    val userId: Int? = null,
    val login: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val color: String = "#2481CC",
    val currentPassword: String = "",
    val newPassword: String = "",
    val isSaving: Boolean = false,
    @param:StringRes val firstNameError: Int? = null,
    @param:StringRes val lastNameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val currentPasswordError: Int? = null,
    @param:StringRes val error: Int? = null,
    val isSaved: Boolean = false,
)
