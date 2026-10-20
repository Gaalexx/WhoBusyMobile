package com.project.whobusy.presentation.register.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.register.viewmodel.RegistrationViewModel

@Composable
fun RegistrationRoot(
    onBack: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegistrationScreen(state, viewModel::onEvent, onBack)
}
