package com.project.whobusy.presentation.login.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.login.viewmodel.LoginViewModel

@Composable
fun LoginRoot(
    onRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    LoginScreen(viewModel.state.collectAsStateWithLifecycle().value, viewModel::onEvent, onRegister)
}
