package com.project.whobusy.presentation.register.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.register.viewmodel.RegistrationViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun RegistrationRoot(
    onBack: () -> Unit,
    viewModel: RegistrationViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegistrationScreen(state, viewModel::onEvent, onBack)
}
