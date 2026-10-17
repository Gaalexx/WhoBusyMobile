package com.project.whobusy.presentation.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData

@Composable
fun ProfileScreen(
    user: CalendarUser,
    onLogout: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background,
            ).testTag("profile-content")
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            Modifier.fillMaxWidth().background(Color.White).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WhoBusyAvatar(user.fullName, calendarColor(user.color), 84.dp)
            Spacer(Modifier.height(20.dp))
            Text(user.fullName, style = MaterialTheme.typography.titleLarge)
            Text(
                user.email,
                Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            stringResource(R.string.profile_calendar_heading),
            Modifier.padding(20.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(R.string.profile_color))
                Text(
                    stringResource(R.string.profile_color_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WhoBusyAvatar("", calendarColor(user.color), 24.dp)
        }
        Text(
            stringResource(
                R.string.profile_role,
                stringResource(
                    if (user.role ==
                        UserRole.ADMIN
                    ) {
                        R.string.profile_role_admin
                    } else {
                        R.string.profile_role_user
                    },
                ),
            ),
            Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.profile_registration_info),
            Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(48.dp))
        WhoBusyButton(
            stringResource(R.string.action_logout),
            onLogout,
            Modifier.padding(16.dp),
            style = ButtonStyle.Destructive,
        )
        Spacer(Modifier.height(90.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Preview(widthDp = 390, heightDp = 650)
@Composable
private fun ProfilePreview() {
    WhoBusyTheme { ProfileScreen(CalendarPreviewData.user, {}) }
}
