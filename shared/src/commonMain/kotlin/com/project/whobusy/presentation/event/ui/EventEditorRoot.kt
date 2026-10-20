package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.desktop.ui.DesktopEventEditorScreen
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel

@Composable
fun EventEditorRoot(
    viewModel: EventEditorViewModel,
    onBack: () -> Unit,
    onVisibility: () -> Unit,
    onDates: () -> Unit,
    onSaved: () -> Unit,
    desktopLayout: Boolean = false,
    onSpace: (Int) -> Unit = {},
    desktopSidebar: (@Composable () -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnSaved by rememberUpdatedState(onSaved)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onEvent(EditorCommand.ResultHandled)
            currentOnSaved()
        }
    }
    val canNavigate = {
        val current = viewModel.state.value
        !current.isSaving && !current.isSaved
    }
    if (desktopLayout) {
        DesktopEventEditorScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onBack = { if (canNavigate()) onBack() },
            onSpace = { if (canNavigate()) onSpace(it) },
            sidebar = desktopSidebar,
        )
        return
    }
    EventEditorScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = { if (canNavigate()) onBack() },
        onVisibility = { if (canNavigate()) onVisibility() },
        onDates = { if (canNavigate()) onDates() },
    )
}
