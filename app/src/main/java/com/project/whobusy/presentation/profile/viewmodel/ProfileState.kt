package com.project.whobusy.presentation.profile.viewmodel

import androidx.annotation.StringRes
import com.project.whobusy.domain.model.CalendarUser

data class ProfileState(
    val user: CalendarUser? = null,
    val invitationCount: Int = 0,
    @param:StringRes val error: Int? = null,
)
