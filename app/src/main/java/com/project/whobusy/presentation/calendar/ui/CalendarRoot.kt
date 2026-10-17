package com.project.whobusy.presentation.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun CalendarRoot(viewModel: CalendarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000)
        }
    }
    CalendarScreen(state, viewModel::onEvent, now)
}
