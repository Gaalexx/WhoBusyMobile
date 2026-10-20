package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState

@Composable
fun MemberRoleScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
) {
    var removing by rememberSaveable { mutableStateOf(false) }
    val member = state.member
    val lastAdmin = member?.role == SpaceRole.Admin && state.members.count { it.role == SpaceRole.Admin } == 1
    val canManage =
        state.space?.myRole == SpaceRole.Admin ||
            (state.space?.myRole == SpaceRole.Moderator && member?.role != SpaceRole.Admin)
    val roles =
        SpaceRole.entries
            .filter {
                (state.space?.myRole == SpaceRole.Admin || it != SpaceRole.Admin) &&
                    (!lastAdmin || it == SpaceRole.Admin)
            }.toSet()
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.spaces_member_role_title), onBack) }) {
        Column(Modifier.fillMaxSize()) {
            SpacesFeedback(state.isLoading, state.error)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (member != null) {
                    Spacer(Modifier.height(16.dp))
                    WhoBusyListRow(
                        member.fullName,
                        member.login,
                        leading = { WhoBusyAvatar(member.fullName, calendarColor(member.color)) },
                    )
                    WhoBusySectionTitle(
                        stringResource(R.string.spaces_role_in, state.space?.name.orEmpty()),
                        Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                    )
                    SpaceRoleSelector(
                        state.selectedRole,
                        { onEvent(SpacesCommand.RoleChanged(it)) },
                        roles,
                        enabled =
                            canManage && !state.isSaving,
                    )
                    Text(
                        stringResource(R.string.spaces_role_hint),
                        Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (lastAdmin) {
                        WhoBusyNotice(
                            stringResource(R.string.spaces_last_admin_title),
                            stringResource(R.string.spaces_last_admin_body),
                            Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
            Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 24.dp)) {
                WhoBusyButton(
                    stringResource(R.string.spaces_save_role),
                    { onEvent(SpacesCommand.SaveRole) },
                    enabled =
                        canManage && member != null && state.selectedRole in roles,
                    loading = state.isSaving,
                )
                Spacer(Modifier.height(16.dp))
                WhoBusyButton(
                    stringResource(R.string.spaces_remove_member),
                    { removing = true },
                    style = ButtonStyle.Destructive,
                    enabled =
                        canManage && !lastAdmin && member != null && !state.isSaving,
                )
            }
        }
    }
    if (removing && member != null) {
        AlertDialog(
            onDismissRequest = { removing = false },
            text = { Text(stringResource(R.string.spaces_remove_confirm, member.fullName)) },
            confirmButton = {
                WhoBusyTextButton(
                    stringResource(R.string.spaces_remove_member),
                    {
                        removing = false
                        onEvent(SpacesCommand.RemoveMember)
                    },
                    color = MaterialTheme.colorScheme.error
                )
            },
            dismissButton = { WhoBusyTextButton(stringResource(R.string.spaces_cancel), { removing = false }) },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun MemberRolePreview() {
    WhoBusyTheme {
        MemberRoleScreen(
            SpacesPreviewData.state.copy(memberId = 5, selectedRole = SpaceRole.Actor),
            {},
            {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LastAdminRolePreview() {
    WhoBusyTheme {
        MemberRoleScreen(
            SpacesPreviewData.state.copy(memberId = 1, selectedRole = SpaceRole.Admin),
            {},
            {},
        )
    }
}
