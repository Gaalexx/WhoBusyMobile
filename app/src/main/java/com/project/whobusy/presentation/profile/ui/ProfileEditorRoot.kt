package com.project.whobusy.presentation.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorEffect
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorViewModel

@Composable
fun ProfileEditorRoot(
    onBack: () -> Unit,
    viewModel: ProfileEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileEditorEffect.Saved -> currentOnBack()
            }
        }
    }
    ProfileEditorScreen(state, viewModel::onEvent, onBack)
}
