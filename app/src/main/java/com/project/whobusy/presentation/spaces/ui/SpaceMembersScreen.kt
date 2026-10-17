package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
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

@Composable
fun SpaceMembersScreen(
    state: SpacesState,
    onBack: () -> Unit,
    onInvite: () -> Unit,
    onMember: (Int) -> Unit,
) {
    val space = state.space
    val locale = LocalConfiguration.current.locales[0]
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(R.string.spaces_members), onBack,
                action = {
                    if (space?.myRole?.canManage ==
                        true
                    ) {
                        WhoBusyIconButton(
                            UiIcon.Add,
                            stringResource(R.string.spaces_invite_member),
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
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                WhoBusySectionTitle(
                    stringResource(
                        R.string.spaces_members_header,
                        space?.name.orEmpty().uppercase(locale),
                        state.members.size,
                    ),
                    Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                )
                state.members.forEach { member ->
                    val canManage =
                        space?.myRole == SpaceRole.Admin ||
                            (space?.myRole == SpaceRole.Moderator && member.role != SpaceRole.Admin)
                    val subtitle =
                        stringResource(R.string.spaces_member_subtitle, member.login, spaceRoleTitle(member.role))
                    WhoBusyListRow(
                        member.fullName,
                        if (member.userId ==
                            state.currentUserId
                        ) {
                            stringResource(R.string.spaces_member_self, subtitle)
                        } else {
                            subtitle
                        },
                        onClick = if (canManage) ({ onMember(member.userId) }) else null,
                        leading = { WhoBusyAvatar(member.fullName, calendarColor(member.color)) },
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
            if (space?.myRole?.canManage == true) {
                WhoBusyButton(
                    stringResource(R.string.spaces_invite_member),
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
