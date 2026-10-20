package com.project.whobusy.presentation.spaces.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.presentation.desktop.ui.DesktopSpacesScreen
import com.project.whobusy.presentation.platform.PlatformBackHandler
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesEffect
import com.project.whobusy.presentation.spaces.viewmodel.SpacesViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun SpacesRoot(
    onOpenSpace: (Int) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onInvitations: () -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
    desktopLayout: Boolean = false,
) {
    LaunchedEffect(viewModel) { viewModel.onEvent(SpacesCommand.Load()) }
    if (desktopLayout) {
        DesktopSpacesScreen(
            viewModel.state.collectAsStateWithLifecycle().value,
            viewModel::onEvent,
            onOpenSpace,
            onCreate,
            onJoin,
            onInvitations,
        )
        return
    }
    SpacesScreen(viewModel.state.collectAsStateWithLifecycle().value, onOpenSpace, onCreate, onJoin, onInvitations)
}

@Composable
fun CreateSpaceRoot(
    onBack: () -> Unit,
    onOpenSpace: (Int) -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
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
    viewModel: SpacesViewModel = metroViewModel(),
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
    viewModel: SpacesViewModel = metroViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel) { viewModel.onEvent(SpacesCommand.Load()) }
    SpacesEffects(viewModel, onOpenSpace = onOpenSpace)
    InvitationsScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}

@Composable
internal fun SpacesEffects(
    viewModel: SpacesViewModel,
    onOpenSpace: (Int) -> Unit = {},
    onMemberSaved: () -> Unit = {},
    onLeft: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val onOpenSpaceUpdated by rememberUpdatedState(onOpenSpace)
    val onMemberSavedUpdated by rememberUpdatedState(onMemberSaved)
    val onLeftUpdated by rememberUpdatedState(onLeft)
    val onDeletedUpdated by rememberUpdatedState(onDeleted)
    val result = state.result
    PlatformBackHandler(state.isSaving) {}
    LaunchedEffect(viewModel, result) {
        if (result != null) {
            viewModel.onEvent(SpacesCommand.ResultHandled)
            when (result) {
                is SpacesEffect.OpenSpace -> onOpenSpaceUpdated(result.spaceId)
                SpacesEffect.MemberSaved -> onMemberSavedUpdated()
                SpacesEffect.Left -> onLeftUpdated()
                SpacesEffect.Deleted -> onDeletedUpdated()
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
internal fun rememberSpacesEventHandler(viewModel: SpacesViewModel): (SpacesCommand) -> Unit {
    val clipboard by rememberUpdatedState(LocalClipboardManager.current)
    return remember(viewModel) {
        { command ->
            if (command == SpacesCommand.CopyCode) {
                val state = viewModel.state.value
                if (!state.isLoading && !state.isSaving && state.result == null) {
                    state.space?.inviteCode?.let { code ->
                        clipboard.setText(AnnotatedString(code))
                        viewModel.onEvent(command)
                    }
                }
            } else {
                viewModel.onEvent(command)
            }
        }
    }
}
