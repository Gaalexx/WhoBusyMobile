package com.project.whobusy.presentation.profile.ui

import androidx.annotation.StringRes
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData

@Composable
fun ProfileScreen(
    user: CalendarUser,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onInvitations: () -> Unit = {},
    onAdmin: () -> Unit = {},
    invitationCount: Int = 0,
    @StringRes error: Int? = null,
) {
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
            stringResource(R.string.auth_profile_details),
            stringResource(R.string.auth_profile_details_description),
            onClick = onEditProfile,
        )
        Spacer(Modifier.height(2.dp))
        WhoBusyListRow(
            stringResource(R.string.profile_color),
            stringResource(R.string.auth_profile_color_description),
            onClick = onEditProfile,
        )
        Spacer(Modifier.height(2.dp))
        WhoBusyListRow(
            stringResource(R.string.auth_profile_invitations),
            if (invitationCount == 0) {
                stringResource(R.string.auth_profile_invitations_empty)
            } else {
                stringResource(R.string.auth_profile_invitations_pending, invitationCount)
            },
            onClick = onInvitations,
        )
        if (user.role == UserRole.ADMIN) {
            Spacer(Modifier.height(2.dp))
            WhoBusyListRow(
                stringResource(R.string.auth_profile_admin),
                stringResource(R.string.auth_profile_admin_description),
                onClick = onAdmin,
            )
        }
        error?.let {
            Text(stringResource(it), Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
        }
        Text(
            stringResource(R.string.auth_profile_roles_hint),
            Modifier.padding(start = 20.dp, end = 20.dp, top = 42.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(48.dp))
        WhoBusyButton(
            stringResource(R.string.action_logout),
            onLogout,
            Modifier.padding(horizontal = 16.dp),
            style = ButtonStyle.Destructive,
        )
        Spacer(Modifier.height(104.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ProfilePreview() {
    WhoBusyTheme {
        WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.screen_profile)) }) {
            ProfileScreen(CalendarPreviewData.user, {}, invitationCount = 2)
        }
    }
}

@Preview(name = "Профиль администратора", widthDp = 390, heightDp = 844)
@Composable
private fun AdminProfilePreview() {
    WhoBusyTheme {
        WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.screen_profile)) }) {
            ProfileScreen(CalendarPreviewData.user.copy(role = UserRole.ADMIN), {})
        }
    }
}
