package com.project.whobusy.presentation.calendars.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.screen_calendars
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarsRoot(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.screen_calendars)) }) {
        CalendarsScreen(state, viewModel::onEvent)
    }
}
