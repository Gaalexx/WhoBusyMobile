package com.project.whobusy.presentation.spaces.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.domain.model.CalendarEvent
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
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel, spaceId) { viewModel.onEvent(SpacesCommand.Load(spaceId)) }
    SpaceDetailsScreen(
        state,
        viewModel::onEvent,
        onBack,
        onMembers,
        onInvite,
        onSettings,
        onCreateEvent,
        onOpenEvent,
        onLeave,
    )
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
