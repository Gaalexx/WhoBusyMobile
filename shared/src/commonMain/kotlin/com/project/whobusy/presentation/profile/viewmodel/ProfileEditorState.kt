package com.project.whobusy.presentation.profile.viewmodel

import org.jetbrains.compose.resources.StringResource

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
    val firstNameError: StringResource? = null,
    val lastNameError: StringResource? = null,
    val emailError: StringResource? = null,
    val passwordError: StringResource? = null,
    val currentPasswordError: StringResource? = null,
    val error: StringResource? = null,
    val isSaved: Boolean = false,
)
