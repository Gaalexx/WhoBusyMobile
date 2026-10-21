package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusySectionCard
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.auth_first_name
import com.project.whobusy.resources.auth_last_name
import com.project.whobusy.resources.auth_login
import com.project.whobusy.resources.auth_profile_color_description
import com.project.whobusy.resources.auth_profile_details
import com.project.whobusy.resources.auth_profile_edit
import com.project.whobusy.resources.desktop_profile_not_specified
import com.project.whobusy.resources.profile_color
import com.project.whobusy.resources.ui_email
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopProfileDetailsPanel(user: CalendarUser, onEditProfile: () -> Unit, enabled: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        WhoBusySectionCard(stringResource(Res.string.auth_profile_details), UiIcon.Profile) {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                    ProfileDetail(stringResource(Res.string.auth_first_name), user.firstName)
                    ProfileDetail(stringResource(Res.string.auth_last_name), user.lastName)
                    ProfileDetail(stringResource(Res.string.auth_login), user.login)
                    ProfileDetail(stringResource(Res.string.ui_email), user.email.orEmpty())
                }
            }
            WhoBusyButton(stringResource(Res.string.auth_profile_edit), onEditProfile, enabled = enabled)
        }
        WhoBusySectionCard(stringResource(Res.string.profile_color), UiIcon.Calendar) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(44.dp).background(calendarColor(user.color), CircleShape))
                Text(
                    stringResource(Res.string.auth_profile_color_description),
                    Modifier.weight(1f),
                    color = DesktopPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ProfileDetail(label: String, value: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = DesktopPalette.Muted, style = MaterialTheme.typography.bodyMedium)
        Text(
            value.ifBlank { stringResource(Res.string.desktop_profile_not_specified) },
            color = if (value.isBlank()) DesktopPalette.Muted else DesktopPalette.Text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(widthDp = 440, heightDp = 720)
@Composable
private fun DesktopProfileDetailsPreview() {
    WhoBusyTheme { DesktopProfileDetailsPanel(CalendarPreviewData.user, {}, enabled = true) }
}

@Preview(name = "Нет email и фамилии", widthDp = 360, heightDp = 720)
@Composable
private fun DesktopProfileOptionalDetailsPreview() {
    WhoBusyTheme {
        DesktopProfileDetailsPanel(CalendarPreviewData.user.copy(email = null, lastName = ""), {}, enabled = false)
    }
}

@Preview(widthDp = 360, showBackground = true)
@Composable
private fun ProfileDetailPreview() {
    WhoBusyTheme {
        Box(Modifier.padding(24.dp)) {
            ProfileDetail(stringResource(Res.string.ui_email), "alex@example.com")
        }
    }
}
