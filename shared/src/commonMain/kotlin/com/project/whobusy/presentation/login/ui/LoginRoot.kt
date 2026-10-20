package com.project.whobusy.presentation.login.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.login.viewmodel.LoginViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun LoginRoot(
    onRegister: () -> Unit,
    viewModel: LoginViewModel = metroViewModel(),
) {
    LoginScreen(viewModel.state.collectAsStateWithLifecycle().value, viewModel::onEvent, onRegister)
}
