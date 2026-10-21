package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusySectionCard
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.profile.viewmodel.ProfileCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_logout
import com.project.whobusy.resources.auth_logout_all
import com.project.whobusy.resources.auth_logout_all_description
import com.project.whobusy.resources.auth_profile_invitations
import com.project.whobusy.resources.auth_profile_invitations_empty
import com.project.whobusy.resources.auth_profile_invitations_pending
import com.project.whobusy.resources.desktop_profile_invitations_body
import com.project.whobusy.resources.desktop_profile_open_invitations
import com.project.whobusy.resources.desktop_profile_password
import com.project.whobusy.resources.desktop_profile_security
import com.project.whobusy.resources.desktop_profile_security_body
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopProfileActionsPanel(
    state: ProfileState,
    onEvent: (ProfileCommand) -> Unit,
    onEditProfile: () -> Unit,
    onInvitations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        WhoBusySectionCard(stringResource(Res.string.auth_profile_invitations), UiIcon.Mail) {
            Text(
                if (state.invitationCount == 0) {
                    stringResource(Res.string.auth_profile_invitations_empty)
                } else {
                    stringResource(Res.string.auth_profile_invitations_pending, state.invitationCount)
                },
                color = DesktopPalette.Accent,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(Res.string.desktop_profile_invitations_body),
                color = DesktopPalette.Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
            WhoBusyButton(
                stringResource(Res.string.desktop_profile_open_invitations),
                onInvitations,
                style = ButtonStyle.Secondary,
                enabled = !state.isLoggingOut,
            )
        }
        WhoBusySectionCard(stringResource(Res.string.desktop_profile_security), UiIcon.Lock) {
            Text(
                stringResource(Res.string.desktop_profile_security_body),
                color = DesktopPalette.Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
            WhoBusyTextButton(
                stringResource(Res.string.desktop_profile_password),
                onEditProfile,
                enabled = !state.isLoggingOut,
            )
            WhoBusyButton(
                stringResource(Res.string.action_logout),
                { onEvent(ProfileCommand.Logout) },
                style = ButtonStyle.Destructive,
                enabled = !state.isLoggingOut,
                loading = state.isLoggingOut && !state.showLogoutAllConfirmation,
            )
            WhoBusyButton(
                stringResource(Res.string.auth_logout_all),
                { onEvent(ProfileCommand.RequestLogoutAll) },
                style = ButtonStyle.Destructive,
                enabled = !state.isLoggingOut,
            )
            Text(
                stringResource(Res.string.auth_logout_all_description),
                color = DesktopPalette.Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun DesktopProfileActionsPreview() {
    WhoBusyTheme {
        DesktopProfileActionsPanel(ProfileState(CalendarPreviewData.user, invitationCount = 3), {}, {}, {})
    }
}

@Preview(name = "Выполняется выход", widthDp = 360, heightDp = 800)
@Composable
private fun DesktopProfileActionsLoadingPreview() {
    WhoBusyTheme {
        DesktopProfileActionsPanel(ProfileState(CalendarPreviewData.user, isLoggingOut = true), {}, {}, {})
    }
}
