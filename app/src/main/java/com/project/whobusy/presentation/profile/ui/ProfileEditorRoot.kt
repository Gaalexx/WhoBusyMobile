package com.project.whobusy.presentation.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorViewModel

@Composable
fun ProfileEditorRoot(
    onBack: () -> Unit,
    viewModel: ProfileEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onEvent(ProfileEditorCommand.ResultHandled)
            currentOnBack()
        }
    }
    ProfileEditorScreen(state, viewModel::onEvent, onBack)
}
