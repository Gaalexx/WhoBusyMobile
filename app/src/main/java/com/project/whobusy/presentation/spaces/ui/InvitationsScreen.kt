package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState

@Composable
fun InvitationsScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
) {
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.spaces_invitations), onBack) }) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item(key = "header") {
                Column {
                    SpacesFeedback(state.isLoading || state.isSaving, state.error)
                    Text(
                        stringResource(R.string.spaces_invitation_hint),
                        Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (state.invitations.isEmpty() && !state.isLoading) {
                item(key = "empty") {
                    Text(stringResource(R.string.spaces_no_invitations), Modifier.padding(20.dp))
                }
            }
            items(
                state.invitations,
                key = { "invitation:${it.id}" },
                contentType = { "invitation" },
            ) { invitation ->
                Column {
                    WhoBusyListRow(
                        title = invitation.spaceName,
                        subtitle = invitation.invitedByName?.let { stringResource(R.string.spaces_invited_by, it) },
                        leading = { WhoBusyAvatar(invitation.spaceName.take(1), spaceColor(invitation.spaceId)) },
                    )
                    Text(
                        stringResource(R.string.spaces_invitation_role, spaceRoleTitle(invitation.role)),
                        Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    WhoBusyButton(
                        stringResource(R.string.spaces_accept),
                        {
                            onEvent(SpacesCommand.AcceptInvitation(invitation.id))
                        },
                        Modifier.padding(horizontal = 16.dp), enabled = !state.isSaving
                    )
                    Spacer(Modifier.height(10.dp))
                    WhoBusyButton(
                        stringResource(R.string.spaces_decline),
                        {
                            onEvent(SpacesCommand.DeclineInvitation(invitation.id))
                        },
                        Modifier.padding(horizontal = 16.dp), style = ButtonStyle.Secondary, enabled = !state.isSaving
                    )
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun InvitationsPreview() {
    WhoBusyTheme { InvitationsScreen(SpacesPreviewData.state, {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EmptyInvitationsPreview() {
    WhoBusyTheme { InvitationsScreen(SpacesState(), {}, {}) }
}
