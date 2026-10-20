package com.project.whobusy.presentation.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun ProfileRoot(
    onEditProfile: () -> Unit,
    onInvitations: () -> Unit,
    onAdmin: () -> Unit,
    viewModel: ProfileViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onEvent(ProfileCommand.Refresh) }
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(R.string.screen_profile),
                action = {
                    WhoBusyIconButton(
                        UiIcon.Edit,
                        stringResource(R.string.auth_profile_edit),
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
