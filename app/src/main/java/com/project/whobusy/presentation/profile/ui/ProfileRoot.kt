package com.project.whobusy.presentation.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel

@Composable
fun ProfileRoot(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar("Профиль") }) {
        state.user?.let { user ->
            ProfileScreen(user, onLogout = { viewModel.onEvent(CalendarCommand.Logout) })
        }
    }
}
