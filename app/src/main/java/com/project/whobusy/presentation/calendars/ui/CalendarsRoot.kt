package com.project.whobusy.presentation.calendars.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel

@Composable
fun CalendarsRoot(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.screen_calendars)) }) {
        CalendarsScreen(state, viewModel::onEvent)
    }
}
