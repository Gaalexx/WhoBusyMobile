package com.project.whobusy.presentation.users.viewmodel

import com.project.whobusy.domain.model.CalendarUser
import org.jetbrains.compose.resources.StringResource

data class UsersState(
    val users: List<CalendarUser> = emptyList(),
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val error: StringResource? = null,
)
