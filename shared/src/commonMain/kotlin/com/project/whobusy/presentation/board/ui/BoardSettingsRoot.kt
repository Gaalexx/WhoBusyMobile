package com.project.whobusy.presentation.board.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsCommand
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsViewModel
import com.project.whobusy.presentation.platform.PlatformBackHandler
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun BoardSettingsRoot(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BoardSettingsViewModel = metroViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val currentOnSaved by rememberUpdatedState(onSaved)
    LaunchedEffect(viewModel) { viewModel.onEvent(BoardSettingsCommand.Load) }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onEvent(BoardSettingsCommand.ResultHandled)
            currentOnSaved()
        }
    }
    PlatformBackHandler(state.isSaving) {}
    BoardSettingsScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}
