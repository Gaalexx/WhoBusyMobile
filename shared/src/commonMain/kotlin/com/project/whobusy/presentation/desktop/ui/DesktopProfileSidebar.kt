package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.admin_role_admin
import com.project.whobusy.resources.admin_role_user
import com.project.whobusy.resources.auth_profile_admin
import com.project.whobusy.resources.auth_profile_details
import com.project.whobusy.resources.auth_profile_invitations
import com.project.whobusy.resources.auth_profile_roles_hint
import com.project.whobusy.resources.screen_profile
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopProfileSidebar(
    user: CalendarUser,
    invitationCount: Int,
    onEditProfile: () -> Unit,
    onInvitations: () -> Unit,
    onAdmin: () -> Unit,
    enabled: Boolean,
) {
    Column(Modifier.width(280.dp).fillMaxHeight().background(Color.White)) {
        Column(
            Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(Res.string.screen_profile), style = MaterialTheme.typography.titleLarge)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WhoBusyAvatar(user.fullName, calendarColor(user.color), 96.dp)
                Text(user.fullName, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(user.login, color = DesktopPalette.Muted, style = MaterialTheme.typography.bodyMedium)
                Surface(color = DesktopPalette.Selection, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        stringResource(
                            if (user.role == UserRole.ADMIN) Res.string.admin_role_admin else Res.string.admin_role_user,
                        ),
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = DesktopPalette.Accent,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            HorizontalDivider(color = DesktopPalette.Border, modifier = Modifier.padding(horizontal = 24.dp))
            WhoBusyListRow(
                title = stringResource(Res.string.auth_profile_details),
                onClick = if (enabled) onEditProfile else null,
                leading = { WhoBusyIcon(UiIcon.Edit, tint = DesktopPalette.Accent) },
                trailing = { WhoBusyIcon(UiIcon.Next) },
            )
            WhoBusyListRow(
                title = stringResource(Res.string.auth_profile_invitations),
                onClick = if (enabled) onInvitations else null,
                leading = { WhoBusyIcon(UiIcon.Mail, tint = DesktopPalette.Accent) },
                trailing = {
                    if (invitationCount > 0) {
                        Surface(color = DesktopPalette.Selection, shape = RoundedCornerShape(20.dp)) {
                            Text(
                                invitationCount.toString(),
                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = DesktopPalette.Accent,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    } else {
                        WhoBusyIcon(UiIcon.Next)
                    }
                },
            )
            if (user.role == UserRole.ADMIN) {
                WhoBusyListRow(
                    title = stringResource(Res.string.auth_profile_admin),
                    onClick = if (enabled) onAdmin else null,
                    leading = { WhoBusyIcon(UiIcon.People, tint = DesktopPalette.Accent) },
                    trailing = { WhoBusyIcon(UiIcon.Next) },
                )
            }
            Text(
                stringResource(Res.string.auth_profile_roles_hint),
                Modifier.padding(24.dp),
                color = DesktopPalette.Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(widthDp = 280, heightDp = 800)
@Preview(name = "Низкое окно", widthDp = 280, heightDp = 400)
@Composable
private fun DesktopProfileSidebarPreview() {
    WhoBusyTheme {
        DesktopProfileSidebar(CalendarPreviewData.user.copy(role = UserRole.ADMIN), 3, {}, {}, {}, enabled = true)
    }
}
