package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.profile.ui.ProfileLogoutAllDialog
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.auth_error_logout
import com.project.whobusy.resources.desktop_profile_subtitle
import com.project.whobusy.resources.desktop_profile_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DesktopProfileScreen(
    state: ProfileState,
    onEvent: (ProfileCommand) -> Unit,
    onEditProfile: () -> Unit,
    onInvitations: () -> Unit,
    onAdmin: () -> Unit,
) {
    val user = state.user ?: return
    Row(Modifier.fillMaxSize().background(DesktopPalette.Background).testTag("desktop-profile")) {
        DesktopProfileSidebar(user, state.invitationCount, onEditProfile, onInvitations, onAdmin, !state.isLoggingOut)
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Column(
                Modifier.fillMaxWidth().height(80.dp).background(DesktopPalette.Rail).padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(Res.string.desktop_profile_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(Res.string.desktop_profile_subtitle),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    Modifier.widthIn(max = 1240.dp).fillMaxWidth().padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    state.error?.let {
                        Text(
                            stringResource(it),
                            Modifier.testTag("desktop-profile-error"),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Box(Modifier.weight(1.1f)) {
                            DesktopProfileDetailsPanel(user, onEditProfile, enabled = !state.isLoggingOut)
                        }
                        DesktopProfileActionsPanel(state, onEvent, onEditProfile, onInvitations, Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (state.showLogoutAllConfirmation) {
        ProfileLogoutAllDialog(
            isLoggingOut = state.isLoggingOut,
            onCancel = { onEvent(ProfileCommand.CancelLogoutAll) },
            onConfirm = { onEvent(ProfileCommand.ConfirmLogoutAll) },
        )
    }
}

@Preview(widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopProfilePreview() {
    WhoBusyTheme { DesktopProfileScreen(ProfileState(CalendarPreviewData.user, invitationCount = 2), {}, {}, {}, {}) }
}

@Preview(name = "Администратор · низкое окно", widthDp = 1080, heightDp = 600)
@Composable
private fun DesktopAdminProfilePreview() {
    WhoBusyTheme {
        DesktopProfileScreen(
            ProfileState(CalendarPreviewData.user.copy(role = UserRole.ADMIN, email = null), invitationCount = 12),
            {}, {}, {}, {},
        )
    }
}

@Preview(name = "Ошибка выхода", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopProfileErrorPreview() {
    WhoBusyTheme {
        DesktopProfileScreen(
            ProfileState(CalendarPreviewData.user, error = Res.string.auth_error_logout),
            {}, {}, {}, {},
        )
    }
}

@Preview(name = "Подтверждение выхода", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopProfileLogoutAllPreview() {
    WhoBusyTheme {
        DesktopProfileScreen(ProfileState(CalendarPreviewData.user, showLogoutAllConfirmation = true), {}, {}, {}, {})
    }
}
