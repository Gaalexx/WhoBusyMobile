package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyLogo
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.navigation.ScreenTypes
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.screen_calendar
import com.project.whobusy.resources.screen_calendars
import com.project.whobusy.resources.screen_profile
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopNavigationRail(
    currentScreen: ScreenTypes?,
    user: CalendarUser?,
    onSelect: (ScreenTypes) -> Unit,
    enabled: Boolean = true,
) {
    val spacesSelected = currentScreen is ScreenTypes.Calendars || currentScreen is ScreenTypes.SpaceDetails ||
        currentScreen is ScreenTypes.SpaceMembers || currentScreen is ScreenTypes.MemberRole ||
        currentScreen is ScreenTypes.InviteMember || currentScreen is ScreenTypes.SpaceSettings ||
        currentScreen is ScreenTypes.LeaveSpace || currentScreen is ScreenTypes.CreateSpace ||
        currentScreen is ScreenTypes.JoinSpace || currentScreen is ScreenTypes.Invitations
    val profileSelected = currentScreen is ScreenTypes.Profile || currentScreen is ScreenTypes.ProfileEditor ||
        currentScreen is ScreenTypes.Users || currentScreen is ScreenTypes.UserEditor
    Column(
        Modifier.width(80.dp).fillMaxHeight().background(DesktopPalette.Rail)
            .padding(horizontal = 4.dp, vertical = 22.dp).testTag("desktop-navigation"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WhoBusyLogo(Modifier.size(56.dp))
        Spacer(Modifier.height(48.dp))
        DesktopRailItem(
            stringResource(Res.string.screen_calendar), UiIcon.Calendar,
            !spacesSelected && !profileSelected,
            enabled,
        ) { onSelect(ScreenTypes.Calendar) }
        Spacer(Modifier.height(18.dp))
        DesktopRailItem(
            stringResource(Res.string.screen_calendars), UiIcon.People,
            spacesSelected,
            enabled,
        ) { onSelect(ScreenTypes.Calendars) }
        Spacer(Modifier.height(18.dp))
        DesktopRailItem(stringResource(Res.string.screen_profile), UiIcon.Profile, profileSelected, enabled) {
            onSelect(ScreenTypes.Profile)
        }
        Spacer(Modifier.weight(1f))
        Column(
            Modifier.clip(RoundedCornerShape(22.dp)).alpha(if (enabled) 1f else 0.45f)
                .clickable(enabled = enabled) { onSelect(ScreenTypes.Profile) },
        ) {
            WhoBusyAvatar(user?.fullName.orEmpty().ifBlank { "W" }, DesktopPalette.Accent, 40.dp)
        }
    }
}

@Composable
private fun DesktopRailItem(label: String, icon: UiIcon, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(14.dp))
            .alpha(if (enabled) 1f else 0.45f)
            .background(if (selected) DesktopPalette.Accent else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WhoBusyIcon(icon, tint = Color.White)
        Text(
            label,
            color = Color.White,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview(widthDp = 80, heightDp = 960)
@Composable
private fun DesktopNavigationPreview() {
    WhoBusyTheme { DesktopNavigationRail(ScreenTypes.Calendar, CalendarPreviewData.state.user, {}) }
}

@Preview(name = "Пространства", widthDp = 80, heightDp = 960)
@Composable
private fun DesktopSpacesNavigationPreview() {
    WhoBusyTheme { DesktopNavigationRail(ScreenTypes.Calendars, CalendarPreviewData.state.user, {}) }
}
