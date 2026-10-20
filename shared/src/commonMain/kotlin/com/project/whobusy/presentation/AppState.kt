package com.project.whobusy.presentation

import com.project.whobusy.domain.model.CalendarUser

enum class AppStartup { Loading, Ready, Failed }

data class AppState(
    val user: CalendarUser? = null,
    val startup: AppStartup = AppStartup.Loading,
)
