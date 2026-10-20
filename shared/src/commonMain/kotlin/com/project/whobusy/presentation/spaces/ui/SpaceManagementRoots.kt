package com.project.whobusy.presentation.spaces.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun InviteMemberRoot(
    spaceId: Int,
    onBack: () -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel, spaceId) { viewModel.onEvent(SpacesCommand.Load(spaceId)) }
    SpacesEffects(viewModel)
    InviteMemberScreen(state, rememberSpacesEventHandler(viewModel), { if (!state.isSaving) onBack() })
}

@Composable
fun SpaceSettingsRoot(
    spaceId: Int,
    onBack: () -> Unit,
    onLeave: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel, spaceId) { viewModel.onEvent(SpacesCommand.Load(spaceId)) }
    SpacesEffects(viewModel, onDeleted = onDeleted)
    SpaceSettingsScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() }, onLeave)
}

@Composable
fun LeaveSpaceRoot(
    spaceId: Int,
    onBack: () -> Unit,
    onLeft: () -> Unit,
    onSettings: () -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel, spaceId) { viewModel.onEvent(SpacesCommand.Load(spaceId)) }
    SpacesEffects(viewModel, onLeft = onLeft)
    LeaveSpaceScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() }, onSettings)
}
