package com.project.whobusy.presentation.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileRoot(
    onEditProfile: () -> Unit,
    onInvitations: () -> Unit,
    onAdmin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
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
                        onEditProfile,
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
                onEditProfile = onEditProfile,
                onInvitations = onInvitations,
                onAdmin = onAdmin,
                invitationCount = state.invitationCount,
                error = state.error,
            )
        }
    }
}
