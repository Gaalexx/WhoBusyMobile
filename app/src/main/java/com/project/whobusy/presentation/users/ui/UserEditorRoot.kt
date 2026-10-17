package com.project.whobusy.presentation.users.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.users.viewmodel.UserEditorCommand
import com.project.whobusy.presentation.users.viewmodel.UserEditorViewModel

@Composable
fun UserEditorRoot(
    userId: Int?,
    onBack: () -> Unit,
    viewModel: UserEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(userId) { viewModel.onEvent(UserEditorCommand.Load(userId)) }
    LaunchedEffect(viewModel) { viewModel.effects.collect { currentOnBack() } }
    UserEditorScreen(state, viewModel::onEvent, onBack)
}
