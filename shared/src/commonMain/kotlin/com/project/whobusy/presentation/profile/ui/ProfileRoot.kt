package com.project.whobusy.presentation.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.presentation.desktop.ui.DesktopProfileScreen
import com.project.whobusy.presentation.platform.PlatformBackHandler
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileViewModel
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.auth_profile_edit
import com.project.whobusy.resources.screen_profile
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileRoot(
    onEditProfile: () -> Unit,
    onInvitations: () -> Unit,
    onAdmin: () -> Unit,
    viewModel: ProfileViewModel = metroViewModel(),
    desktopLayout: Boolean = false,
    onNavigationBlockedChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnNavigationBlockedChanged by rememberUpdatedState(onNavigationBlockedChanged)
    SideEffect { currentOnNavigationBlockedChanged(state.isLoggingOut) }
    DisposableEffect(viewModel) {
        onDispose { currentOnNavigationBlockedChanged(false) }
    }
    LaunchedEffect(Unit) { viewModel.onEvent(ProfileCommand.Refresh) }
    if (desktopLayout) {
        PlatformBackHandler(state.isLoggingOut) {}
        DesktopProfileScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onEditProfile = { if (!viewModel.state.value.isLoggingOut) onEditProfile() },
            onInvitations = { if (!viewModel.state.value.isLoggingOut) onInvitations() },
            onAdmin = { if (!viewModel.state.value.isLoggingOut) onAdmin() },
        )
        return
    }
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(Res.string.screen_profile),
                action = {
                    WhoBusyIconButton(
                        UiIcon.Edit,
                        stringResource(Res.string.auth_profile_edit),
                        { if (!viewModel.state.value.isLoggingOut) onEditProfile() },
                        tint = Color.White,
                    )
                }
            )
        },
    ) {
        state.user?.let { user ->
            ProfileScreen(
                user = user,
                onLogout = { viewModel.onEvent(ProfileCommand.Logout) },
                onEditProfile = { if (!viewModel.state.value.isLoggingOut) onEditProfile() },
                onInvitations = { if (!viewModel.state.value.isLoggingOut) onInvitations() },
                onAdmin = { if (!viewModel.state.value.isLoggingOut) onAdmin() },
                invitationCount = state.invitationCount,
                error = state.error,
                onRequestLogoutAll = { viewModel.onEvent(ProfileCommand.RequestLogoutAll) },
                onCancelLogoutAll = { viewModel.onEvent(ProfileCommand.CancelLogoutAll) },
                onConfirmLogoutAll = { viewModel.onEvent(ProfileCommand.ConfirmLogoutAll) },
                showLogoutAllConfirmation = state.showLogoutAllConfirmation,
                isLoggingOut = state.isLoggingOut,
            )
        }
    }
}
