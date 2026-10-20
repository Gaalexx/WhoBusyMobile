package com.project.whobusy.presentation.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.platform.PlatformBackHandler
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.action_logout
import com.project.whobusy.resources.auth_logout_all
import com.project.whobusy.resources.auth_logout_all_confirmation
import com.project.whobusy.resources.auth_logout_all_description
import com.project.whobusy.resources.auth_profile_admin
import com.project.whobusy.resources.auth_profile_admin_description
import com.project.whobusy.resources.auth_profile_color_description
import com.project.whobusy.resources.auth_profile_details
import com.project.whobusy.resources.auth_profile_details_description
import com.project.whobusy.resources.auth_profile_invitations
import com.project.whobusy.resources.auth_profile_invitations_empty
import com.project.whobusy.resources.auth_profile_invitations_pending
import com.project.whobusy.resources.auth_profile_roles_hint
import com.project.whobusy.resources.profile_color
import com.project.whobusy.resources.screen_profile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
    user: CalendarUser,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onInvitations: () -> Unit = {},
    onAdmin: () -> Unit = {},
    invitationCount: Int = 0,
    error: StringResource? = null,
    onRequestLogoutAll: () -> Unit = {},
    onCancelLogoutAll: () -> Unit = {},
    onConfirmLogoutAll: () -> Unit = {},
    showLogoutAllConfirmation: Boolean = false,
    isLoggingOut: Boolean = false,
) {
    PlatformBackHandler(enabled = isLoggingOut) {}
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile-content")
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            Modifier.fillMaxWidth().background(Color.White).padding(top = 20.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WhoBusyAvatar(user.fullName, calendarColor(user.color), 84.dp)
            Spacer(Modifier.height(18.dp))
            Text(user.fullName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text(
                user.login,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(24.dp))
        WhoBusyListRow(
            stringResource(Res.string.auth_profile_details),
            stringResource(Res.string.auth_profile_details_description),
            onClick = if (isLoggingOut) null else onEditProfile,
        )
        Spacer(Modifier.height(2.dp))
        WhoBusyListRow(
            stringResource(Res.string.profile_color),
            stringResource(Res.string.auth_profile_color_description),
            onClick = if (isLoggingOut) null else onEditProfile,
        )
        Spacer(Modifier.height(2.dp))
        WhoBusyListRow(
            stringResource(Res.string.auth_profile_invitations),
            if (invitationCount == 0) {
                stringResource(Res.string.auth_profile_invitations_empty)
            } else {
                stringResource(Res.string.auth_profile_invitations_pending, invitationCount)
            },
            onClick = if (isLoggingOut) null else onInvitations,
        )
        if (user.role == UserRole.ADMIN) {
            Spacer(Modifier.height(2.dp))
            WhoBusyListRow(
                stringResource(Res.string.auth_profile_admin),
                stringResource(Res.string.auth_profile_admin_description),
                onClick = if (isLoggingOut) null else onAdmin,
            )
        }
        error?.let {
            Text(stringResource(it), Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
        }
        Text(
            stringResource(Res.string.auth_profile_roles_hint),
            Modifier.padding(start = 20.dp, end = 20.dp, top = 42.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(48.dp))
        WhoBusyButton(
            stringResource(Res.string.action_logout),
            onLogout,
            Modifier.padding(horizontal = 16.dp),
            style = ButtonStyle.Destructive,
            enabled = !isLoggingOut,
            loading = isLoggingOut && !showLogoutAllConfirmation,
        )
        Spacer(Modifier.height(12.dp))
        WhoBusyButton(
            stringResource(Res.string.auth_logout_all),
            onRequestLogoutAll,
            Modifier.padding(horizontal = 16.dp),
            style = ButtonStyle.Destructive,
            enabled = !isLoggingOut,
        )
        Spacer(Modifier.height(104.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
    if (showLogoutAllConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!isLoggingOut) onCancelLogoutAll() },
            title = { Text(stringResource(Res.string.auth_logout_all_confirmation)) },
            text = { Text(stringResource(Res.string.auth_logout_all_description)) },
            confirmButton = {
                WhoBusyTextButton(
                    text = stringResource(Res.string.action_logout),
                    onClick = onConfirmLogoutAll,
                    enabled = !isLoggingOut,
                    loading = isLoggingOut,
                )
            },
            dismissButton = {
                WhoBusyTextButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onCancelLogoutAll,
                    enabled = !isLoggingOut,
                )
            },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ProfilePreview() {
    WhoBusyTheme {
        WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.screen_profile)) }) {
            ProfileScreen(CalendarPreviewData.user, {}, invitationCount = 2)
        }
    }
}

@Preview(name = "Профиль администратора", widthDp = 390, heightDp = 844)
@Composable
private fun AdminProfilePreview() {
    WhoBusyTheme {
        WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.screen_profile)) }) {
            ProfileScreen(CalendarPreviewData.user.copy(role = UserRole.ADMIN), {})
        }
    }
}

@Preview(name = "Выход на всех устройствах", widthDp = 390, heightDp = 844)
@Composable
private fun LogoutAllConfirmationPreview() {
    WhoBusyTheme {
        WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.screen_profile)) }) {
            ProfileScreen(CalendarPreviewData.user, {}, showLogoutAllConfirmation = true)
        }
    }
}

@Preview(name = "Выполняется выход на всех устройствах", widthDp = 390, heightDp = 844)
@Composable
private fun LogoutAllLoadingPreview() {
    WhoBusyTheme {
        WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.screen_profile)) }) {
            ProfileScreen(CalendarPreviewData.user, {}, showLogoutAllConfirmation = true, isLoggingOut = true)
        }
    }
}
