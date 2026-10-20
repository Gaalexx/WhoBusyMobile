package com.project.whobusy.presentation.users.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.users.viewmodel.UserEditorCommand
import com.project.whobusy.presentation.users.viewmodel.UserEditorViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun UserEditorRoot(
    userId: Int?,
    onBack: () -> Unit,
    viewModel: UserEditorViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(userId) { viewModel.onEvent(UserEditorCommand.Load(userId)) }
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            viewModel.onEvent(UserEditorCommand.ResultHandled)
            currentOnBack()
        }
    }
    UserEditorScreen(state, viewModel::onEvent, onBack)
}
