package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel

@Composable
fun EventEditorRoot(
    viewModel: EventEditorViewModel,
    onBack: () -> Unit,
    onVisibility: () -> Unit,
    onDates: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventEditorScreen(state, viewModel::onEvent, onBack, onVisibility, onDates)
}
