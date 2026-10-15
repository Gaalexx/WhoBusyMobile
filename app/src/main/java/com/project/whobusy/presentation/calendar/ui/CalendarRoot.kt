package com.project.whobusy.presentation.calendar.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.calendar.model.CalendarTab
import com.project.whobusy.presentation.calendar.viewmodel.*
import com.project.whobusy.presentation.event.ui.EventOverlay
import java.time.LocalDateTime
import kotlinx.coroutines.delay

@Composable
fun CalendarRoot(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val now by produceState(LocalDateTime.now()) {
        while (true) { value = LocalDateTime.now(); delay(60_000) }
    }
    BackHandler(state.tab != CalendarTab.Calendar) {
        viewModel.onEvent(CalendarCommand.SelectTab(CalendarTab.Calendar))
    }
    CalendarScreen(state, viewModel::onEvent, now)
    EventOverlay(state, viewModel::onEvent)
}
