package com.project.whobusy.presentation.profile.viewmodel

import com.project.whobusy.domain.model.CalendarUser
import org.jetbrains.compose.resources.StringResource

data class ProfileState(
    val user: CalendarUser? = null,
    val invitationCount: Int = 0,
    val showLogoutAllConfirmation: Boolean = false,
    val isLoggingOut: Boolean = false,
    val error: StringResource? = null,
)
