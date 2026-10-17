package com.project.whobusy.presentation.board.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsCommand
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsViewModel

@Composable
fun BoardSettingsRoot(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BoardSettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel) { viewModel.onEvent(BoardSettingsCommand.Load) }
    LaunchedEffect(viewModel) { viewModel.saved.collect { onSaved() } }
    BackHandler(state.isSaving) {}
    BoardSettingsScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}
