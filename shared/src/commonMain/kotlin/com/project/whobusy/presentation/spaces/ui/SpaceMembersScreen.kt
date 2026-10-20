package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_invite_member
import com.project.whobusy.resources.spaces_member_self
import com.project.whobusy.resources.spaces_member_subtitle
import com.project.whobusy.resources.spaces_members
import com.project.whobusy.resources.spaces_members_header
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SpaceMembersScreen(
    state: SpacesState,
    onBack: () -> Unit,
    onInvite: () -> Unit,
    onMember: (Int) -> Unit,
) {
    val space = state.space
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(Res.string.spaces_members), onBack,
                action = {
                    if (space?.myRole?.canManage ==
                        true
                    ) {
                        WhoBusyIconButton(
                            UiIcon.Add,
                            stringResource(Res.string.spaces_invite_member),
                            onInvite,
                            tint = Color.White,
                        )
                    }
                }
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            SpacesFeedback(state.isLoading, state.error)
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                item(key = "header") {
                    WhoBusySectionTitle(
                        stringResource(
                            Res.string.spaces_members_header,
                            space?.name.orEmpty().uppercase(),
                            state.members.size,
                        ),
                        Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    )
                }
                items(state.members, key = { "member:${it.userId}" }, contentType = { "member" }) { member ->
                    val canManage =
                        space?.myRole == SpaceRole.Admin ||
                            (space?.myRole == SpaceRole.Moderator && member.role != SpaceRole.Admin)
                    val subtitle =
                        stringResource(Res.string.spaces_member_subtitle, member.login, spaceRoleTitle(member.role))
                    WhoBusyListRow(
                        member.fullName,
                        if (member.userId ==
                            state.currentUserId
                        ) {
                            stringResource(Res.string.spaces_member_self, subtitle)
                        } else {
                            subtitle
                        },
                        onClick = if (canManage) ({ onMember(member.userId) }) else null,
                        leading = { WhoBusyAvatar(member.fullName, calendarColor(member.color)) },
                    )
                }
            }
            if (space?.myRole?.canManage == true) {
                WhoBusyButton(
                    stringResource(Res.string.spaces_invite_member),
                    onInvite,
                    Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun MembersPreview() {
    WhoBusyTheme { SpaceMembersScreen(SpacesPreviewData.state, {}, {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReadOnlyMembersPreview() {
    WhoBusyTheme { SpaceMembersScreen(SpacesPreviewData.state.copy(spaceId = 3), {}, {}, {}) }
}
