package com.project.whobusy.presentation.spaces.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.desktop.ui.DesktopSpaceDetailsScreen
import com.project.whobusy.presentation.platform.PlatformBackHandler
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun SpaceDetailsRoot(
    spaceId: Int,
    onBack: () -> Unit,
    onMembers: () -> Unit,
    onInvite: () -> Unit,
    onSettings: () -> Unit,
    onCreateEvent: (Int) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
    onLeave: () -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
    desktopLayout: Boolean = false,
    onBoard: () -> Unit = onBack,
    onSpace: (Int) -> Unit = {},
    onMember: (Int) -> Unit = {},
    onCreateSpace: () -> Unit = {},
    onJoinSpace: () -> Unit = {},
    onInvitations: () -> Unit = {},
    onNavigationBlockedChanged: (Boolean) -> Unit = {},
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val currentOnNavigationBlockedChanged by rememberUpdatedState(onNavigationBlockedChanged)
    SideEffect { currentOnNavigationBlockedChanged(state.isSaving || state.result != null) }
    DisposableEffect(viewModel) {
        onDispose { currentOnNavigationBlockedChanged(false) }
    }
    val canNavigate = {
        val current = viewModel.state.value
        !current.isSaving && current.result == null
    }
    PlatformBackHandler(state.isSaving || state.result != null) {}
    LaunchedEffect(viewModel, spaceId) { viewModel.onEvent(SpacesCommand.Load(spaceId)) }
    if (desktopLayout) {
        DesktopSpaceDetailsScreen(
            state = state,
            onEvent = rememberSpacesEventHandler(viewModel),
            onBoard = { if (canNavigate()) onBoard() },
            onSettings = { if (canNavigate()) onSettings() },
            onCreateEvent = { if (canNavigate()) onCreateEvent(it) },
            onLeave = { if (canNavigate()) onLeave() },
            onMember = { if (canNavigate()) onMember(it) },
            onSpace = { if (canNavigate()) onSpace(it) },
            onCreateSpace = { if (canNavigate()) onCreateSpace() },
            onJoinSpace = { if (canNavigate()) onJoinSpace() },
            onInvitations = { if (canNavigate()) onInvitations() },
        )
    } else {
        SpaceDetailsScreen(
            state,
            viewModel::onEvent,
            { if (canNavigate()) onBack() },
            { if (canNavigate()) onMembers() },
            { if (canNavigate()) onInvite() },
            { if (canNavigate()) onSettings() },
            { if (canNavigate()) onCreateEvent(it) },
            { if (canNavigate()) onOpenEvent(it) },
            { if (canNavigate()) onLeave() },
        )
    }

}

@Composable
fun SpaceMembersRoot(
    spaceId: Int,
    onBack: () -> Unit,
    onInvite: () -> Unit,
    onMember: (Int) -> Unit,
    viewModel: SpacesViewModel = metroViewModel(),
) {
    LaunchedEffect(viewModel, spaceId) { viewModel.onEvent(SpacesCommand.Load(spaceId)) }
    SpaceMembersScreen(viewModel.state.collectAsStateWithLifecycle().value, onBack, onInvite, onMember)
}

@Composable
fun MemberRoleRoot(
    spaceId: Int,
    userId: Int,
    onBack: () -> Unit,
    onLeft: () -> Unit = onBack,
    viewModel: SpacesViewModel = metroViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel, spaceId, userId) { viewModel.onEvent(SpacesCommand.Load(spaceId, userId)) }
    SpacesEffects(viewModel, onMemberSaved = onBack, onLeft = onLeft)
    MemberRoleScreen(state, viewModel::onEvent, { if (!state.isSaving) onBack() })
}
