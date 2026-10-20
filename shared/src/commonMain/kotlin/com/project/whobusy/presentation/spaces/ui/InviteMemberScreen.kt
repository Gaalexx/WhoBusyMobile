package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.components.WhoBusyValueButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_cancel
import com.project.whobusy.resources.spaces_cancel_invitation
import com.project.whobusy.resources.spaces_copy_key
import com.project.whobusy.resources.spaces_invite
import com.project.whobusy.resources.spaces_key_hint
import com.project.whobusy.resources.spaces_key_label
import com.project.whobusy.resources.spaces_key_section
import com.project.whobusy.resources.spaces_login
import com.project.whobusy.resources.spaces_login_section
import com.project.whobusy.resources.spaces_pending
import com.project.whobusy.resources.spaces_regenerate_key
import com.project.whobusy.resources.spaces_role_after_accept
import com.project.whobusy.resources.spaces_send_invitation
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun InviteMemberScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
) {
    var selectingRole by rememberSaveable { mutableStateOf(false) }
    val canManage = state.space?.myRole?.canManage == true
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.spaces_invite), onBack) }) {
        LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item(key = "feedback") {
                SpacesFeedback(state.isLoading || state.isSaving, state.error, state.message)
            }
            if (canManage) {
                item(key = "invite-key") {
                    Column {
                        WhoBusySectionTitle(
                            stringResource(Res.string.spaces_key_section),
                            Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        )
                        WhoBusyValueButton(
                            stringResource(Res.string.spaces_key_label), state.space?.inviteCode.orEmpty(),
                            {
                                onEvent(SpacesCommand.CopyCode)
                            },
                            Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        WhoBusyButton(
                            stringResource(Res.string.spaces_copy_key),
                            {
                                onEvent(SpacesCommand.CopyCode)
                            },
                            Modifier.padding(horizontal = 16.dp),
                            style = ButtonStyle.Secondary,
                            enabled = !state.isSaving
                        )
                        WhoBusyTextButton(
                            stringResource(Res.string.spaces_regenerate_key),
                            {
                                onEvent(SpacesCommand.RegenerateCode)
                            },
                            Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            stringResource(Res.string.spaces_key_hint),
                            Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(28.dp))
                    }
                }
                item(key = "invite-form") {
                    Column {
                        WhoBusySectionTitle(
                            stringResource(Res.string.spaces_login_section),
                            Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        )
                        WhoBusyTextField(
                            state.login,
                            {
                                onEvent(SpacesCommand.LoginChanged(it))
                            },
                            stringResource(
                                Res.string.spaces_login,
                            ),
                            Modifier.padding(horizontal = 16.dp),
                            enabled = !state.isSaving,
                        )
                        Spacer(Modifier.height(16.dp))
                        WhoBusyListRow(
                            stringResource(
                                Res.string.spaces_role_after_accept,
                            ),
                            spaceRoleTitle(state.selectedRole),
                            onClick = {
                                if (!state.isSaving) {
                                    selectingRole =
                                        true
                                }
                            },
                        )
                        Spacer(Modifier.height(18.dp))
                        WhoBusyButton(
                            stringResource(Res.string.spaces_send_invitation),
                            {
                                onEvent(SpacesCommand.Invite)
                            },
                            Modifier.padding(horizontal = 16.dp), enabled = !state.isSaving
                        )
                        Spacer(Modifier.height(30.dp))
                    }
                }
                items(
                    state.pendingInvitations,
                    key = { "invitation:${it.id}" },
                    contentType = { "invitation" },
                ) { invitation ->
                    val canCancel = state.space?.myRole == SpaceRole.Admin || invitation.role != SpaceRole.Admin
                    WhoBusyListRow(
                        stringResource(Res.string.spaces_pending, invitation.login),
                        stringResource(Res.string.spaces_cancel_invitation, spaceRoleTitle(invitation.role)),
                        onClick =
                            if (canCancel &&
                                !state.isSaving
                            ) {
                                ({ onEvent(SpacesCommand.CancelInvitation(invitation.id)) })
                            } else {
                                null
                            },
                    )
                }
            }
        }
    }
    if (selectingRole) {
        AlertDialog(
            onDismissRequest = { selectingRole = false },
            title = { Text(stringResource(Res.string.spaces_role_after_accept)) },
            text = {
                SpaceRoleSelector(
                    state.selectedRole,
                    {
                        onEvent(SpacesCommand.RoleChanged(it))
                        selectingRole = false
                    },
                    SpaceRole.entries
                        .filter {
                            state.space?.myRole ==
                                SpaceRole.Admin ||
                                it != SpaceRole.Admin
                        }.toSet(),
                )
            },
            confirmButton = { WhoBusyTextButton(stringResource(Res.string.spaces_cancel), { selectingRole = false }) },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun InviteMemberPreview() {
    WhoBusyTheme {
        InviteMemberScreen(
            SpacesPreviewData.state.copy(
                login = "elena",
                pendingInvitations =
                    listOf(
                        SpacesPreviewData.invitations.first().copy(login = "ivan", role = SpaceRole.Visitor),
                    ),
            ),
            {
            },
            {},
        )
    }
}
