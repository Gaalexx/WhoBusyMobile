package com.project.whobusy.presentation.users.viewmodel

import androidx.annotation.StringRes
import com.project.whobusy.domain.model.CalendarUser

data class UsersState(
    val users: List<CalendarUser> = emptyList(),
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    @param:StringRes val error: Int? = null,
)
