package com.project.whobusy.presentation.spaces.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesEffect
import com.project.whobusy.presentation.spaces.viewmodel.SpacesViewModel

@Composable
fun SpacesRoot(
    onOpenSpace: (Int) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onInvitations: () -> Unit,
    viewModel: SpacesViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) { viewModel.onEvent(SpacesCommand.Load()) }
    SpacesScreen(viewModel.state.collectAsStateWithLifecycle().value, onOpenSpace, onCreate, onJoin, onInvitations)
}

@Composable
fun CreateSpaceRoot(
    onBack: () -> Unit,
    onOpenSpace: (Int) -> Unit,
    viewModel: SpacesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel) { viewModel.onEvent(SpacesCommand.Load()) }
    SpacesEffects(viewModel, onOpenSpace = onOpenSpace)
    CreateSpaceScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}

@Composable
fun JoinSpaceRoot(
    onBack: () -> Unit,
    onOpenSpace: (Int) -> Unit,
    viewModel: SpacesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel) { viewModel.onEvent(SpacesCommand.Load()) }
    SpacesEffects(viewModel, onOpenSpace = onOpenSpace)
    JoinSpaceScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}

@Composable
fun InvitationsRoot(
    onBack: () -> Unit,
    onOpenSpace: (Int) -> Unit,
    viewModel: SpacesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel) { viewModel.onEvent(SpacesCommand.Load()) }
    SpacesEffects(viewModel, onOpenSpace = onOpenSpace)
    InvitationsScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}

@Suppress("DEPRECATION")
@Composable
internal fun SpacesEffects(
    viewModel: SpacesViewModel,
    onOpenSpace: (Int) -> Unit = {},
    onMemberSaved: () -> Unit = {},
    onLeft: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val clipboard = LocalClipboardManager.current
    val state = viewModel.state.collectAsStateWithLifecycle().value
    BackHandler(state.isSaving) {}
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SpacesEffect.OpenSpace -> onOpenSpace(effect.spaceId)
                is SpacesEffect.CopyCode -> clipboard.setText(AnnotatedString(effect.value))
                SpacesEffect.MemberSaved -> onMemberSaved()
                SpacesEffect.Left -> onLeft()
                SpacesEffect.Deleted -> onDeleted()
            }
        }
    }
}
