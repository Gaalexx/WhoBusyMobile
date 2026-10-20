package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.SpaceMember
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.ui.SpacesFeedback
import com.project.whobusy.presentation.spaces.ui.SpacesPreviewData
import com.project.whobusy.presentation.spaces.ui.spaceColor
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.desktop_space_board
import com.project.whobusy.resources.desktop_space_member
import com.project.whobusy.resources.desktop_space_no_members
import com.project.whobusy.resources.desktop_space_no_pending
import com.project.whobusy.resources.desktop_space_role
import com.project.whobusy.resources.desktop_space_role_notice
import com.project.whobusy.resources.desktop_space_role_notice_body
import com.project.whobusy.resources.desktop_space_settings
import com.project.whobusy.resources.desktop_space_summary
import com.project.whobusy.resources.spaces_access_body
import com.project.whobusy.resources.spaces_access_title
import com.project.whobusy.resources.spaces_add_plan
import com.project.whobusy.resources.spaces_cancel_invitation
import com.project.whobusy.resources.spaces_error_unavailable
import com.project.whobusy.resources.spaces_invitations
import com.project.whobusy.resources.spaces_leave
import com.project.whobusy.resources.spaces_member_self
import com.project.whobusy.resources.spaces_members
import com.project.whobusy.resources.spaces_pending
import com.project.whobusy.resources.spaces_retry
import com.project.whobusy.resources.spaces_settings
import com.project.whobusy.resources.spaces_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DesktopSpaceDetailsScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBoard: () -> Unit,
    onSettings: () -> Unit,
    onCreateEvent: (Int) -> Unit,
    onLeave: () -> Unit,
    onMember: (Int) -> Unit,
    onSpace: (Int) -> Unit,
    onCreateSpace: () -> Unit,
    onJoinSpace: () -> Unit,
    onInvitations: () -> Unit,
) {
    val navigationEnabled = !state.isSaving && state.result == null
    Row(Modifier.fillMaxSize()) {
        DesktopSpaceNavigation(
            state.spaces,
            state.spaceId,
            state.invitations.size,
            onSpace,
            onCreateSpace,
            onJoinSpace,
            onInvitations,
            enabled = navigationEnabled,
        )
        Column(Modifier.weight(1f).fillMaxHeight().background(WhoBusyColors.Background)) {
            val space = state.space
            Row(
                Modifier.fillMaxWidth().height(80.dp).background(WhoBusyColors.Header).padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        space?.name ?: stringResource(Res.string.spaces_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    if (space != null) {
                        Text(
                            stringResource(Res.string.desktop_space_summary, space.memberCount, spaceRoleTitle(space.myRole)),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                WhoBusyButton(
                    stringResource(Res.string.desktop_space_board),
                    onBoard,
                    Modifier.width(180.dp),
                    style = ButtonStyle.Secondary,
                    enabled = navigationEnabled,
                )
            }
            SpacesFeedback(state.isLoading || state.isSaving, state.error, state.message)
            if (space == null) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!state.isLoading) {
                        Text(stringResource(Res.string.spaces_error_unavailable))
                        WhoBusyButton(
                            stringResource(Res.string.spaces_retry),
                            { onEvent(SpacesCommand.Load(state.spaceId)) },
                            Modifier.width(180.dp),
                        )
                    }
                }
            } else {
                BoxWithConstraints(Modifier.fillMaxSize().padding(24.dp)) {
                    val panelWidth = if (maxWidth >= 960.dp) 312.dp else 280.dp
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        DesktopSpaceMembersCard(state, onEvent, onSettings, onMember, Modifier.weight(1f))
                        if (space.myRole.canManage) {
                            DesktopSpaceInvitePanel(state, onEvent, Modifier.width(panelWidth).fillMaxHeight())
                        } else {
                            Surface(
                                modifier = Modifier.width(panelWidth).fillMaxHeight(),
                                color = Color.White,
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                    WhoBusyNotice(
                                        stringResource(Res.string.spaces_access_title),
                                        stringResource(Res.string.spaces_access_body),
                                    )
                                    WhoBusyButton(
                                        stringResource(Res.string.spaces_add_plan),
                                        { onCreateEvent(space.id) },
                                        enabled = navigationEnabled,
                                    )
                                    WhoBusyButton(
                                        stringResource(Res.string.spaces_leave),
                                        onLeave,
                                        style = ButtonStyle.Destructive,
                                        enabled = navigationEnabled,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopSpaceMembersCard(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onSettings: () -> Unit,
    onMember: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val space = state.space ?: return
    val navigationEnabled = !state.isSaving && state.result == null
    var selectedTab by rememberSaveable(space.id) { mutableStateOf(0) }
    val activeTab = if (space.myRole.canManage) selectedTab else 0
    Surface(modifier.fillMaxHeight(), color = Color.White, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(24.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WhoBusyAvatar(space.name, spaceColor(space.id), size = 64.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(space.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        spaceRoleTitle(space.myRole),
                        color = WhoBusyColors.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (space.myRole.canManage) {
                    WhoBusyIconButton(
                        UiIcon.More,
                        stringResource(Res.string.spaces_settings),
                        onSettings,
                        enabled = navigationEnabled,
                    )
                }
            }
            TabRow(selectedTabIndex = activeTab, containerColor = Color.White) {
                Tab(activeTab == 0, { selectedTab = 0 }, text = { Text(stringResource(Res.string.spaces_members)) })
                if (space.myRole.canManage) {
                    Tab(activeTab == 1, { selectedTab = 1 }, text = { Text(stringResource(Res.string.spaces_invitations)) })
                    Tab(
                        false,
                        onSettings,
                        enabled = navigationEnabled,
                        text = { Text(stringResource(Res.string.desktop_space_settings)) },
                    )
                }
            }
            if (activeTab == 0) {
                Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 14.dp)) {
                    Text(
                        stringResource(Res.string.desktop_space_member),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = WhoBusyColors.SecondaryText,
                    )
                    Text(
                        stringResource(Res.string.desktop_space_role),
                        Modifier.width(164.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = WhoBusyColors.SecondaryText,
                    )
                }
                LazyColumn(Modifier.weight(1f)) {
                    if (state.members.isEmpty() && !state.isLoading) {
                        item(key = "empty") { Text(stringResource(Res.string.desktop_space_no_members)) }
                    }
                    items(state.members, key = { it.userId }) { member ->
                        DesktopSpaceMemberRow(state, member, onMember)
                    }
                }
            } else {
                LazyColumn(Modifier.weight(1f).padding(top = 20.dp)) {
                    if (state.pendingInvitations.isEmpty()) {
                        item(key = "empty") {
                            Text(stringResource(Res.string.desktop_space_no_pending), color = WhoBusyColors.SecondaryText)
                        }
                    }
                    items(state.pendingInvitations, key = { it.id }) { invitation ->
                        Column(Modifier.padding(vertical = 12.dp)) {
                            Text(stringResource(Res.string.spaces_pending, invitation.login))
                            Text(spaceRoleTitle(invitation.role), color = WhoBusyColors.SecondaryText)
                            if (space.myRole == SpaceRole.Admin || invitation.role != SpaceRole.Admin) {
                                WhoBusyTextButton(
                                    stringResource(Res.string.spaces_cancel_invitation, spaceRoleTitle(invitation.role)),
                                    { onEvent(SpacesCommand.CancelInvitation(invitation.id)) },
                                    enabled = !state.isSaving,
                                )
                            }
                            HorizontalDivider(color = WhoBusyColors.Divider)
                        }
                    }
                }
            }
            WhoBusyNotice(
                stringResource(Res.string.desktop_space_role_notice),
                stringResource(Res.string.desktop_space_role_notice_body),
                Modifier.padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun DesktopSpaceMemberRow(state: SpacesState, member: SpaceMember, onMember: (Int) -> Unit) {
    val canManage = !state.isSaving && state.result == null && (
        state.space?.myRole == SpaceRole.Admin ||
            state.space?.myRole == SpaceRole.Moderator && member.role != SpaceRole.Admin
        )
    Surface(
        onClick = { onMember(member.userId) },
        enabled = canManage,
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WhoBusyAvatar(member.fullName, calendarColor(member.color), size = 44.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(member.fullName)
                    Text(
                        if (member.userId == state.currentUserId) {
                            stringResource(Res.string.spaces_member_self, member.login)
                        } else member.login,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhoBusyColors.SecondaryText,
                    )
                }
                Surface(
                    color = if (member.role == SpaceRole.Admin) WhoBusyColors.BlueTint else WhoBusyColors.Background,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        spaceRoleTitle(member.role),
                        Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (member.role == SpaceRole.Admin) WhoBusyColors.Primary else WhoBusyColors.SecondaryText,
                    )
                }
                if (canManage) WhoBusyIcon(UiIcon.ExpandMore)
            }
            HorizontalDivider(Modifier.padding(start = 60.dp), color = WhoBusyColors.Divider)
        }
    }
}

@Preview(widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopSpaceDetailsPreview() {
    WhoBusyTheme { DesktopSpaceDetailsScreen(SpacesPreviewData.state, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
}

@Preview(name = "Доступ только для чтения", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopReadOnlySpacePreview() {
    WhoBusyTheme {
        DesktopSpaceDetailsScreen(SpacesPreviewData.state.copy(spaceId = 3), {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
