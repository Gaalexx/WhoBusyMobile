package com.project.whobusy.presentation.users.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.users.viewmodel.UsersCommand
import com.project.whobusy.presentation.users.viewmodel.UsersViewModel

@Composable
fun UsersRoot(
    onBack: () -> Unit,
    onEdit: (Int?) -> Unit,
    viewModel: UsersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onEvent(UsersCommand.Refresh) }
    UsersScreen(state, onBack, onEdit, onRefresh = { viewModel.onEvent(UsersCommand.Refresh) })
}
