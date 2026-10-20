package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_create
import com.project.whobusy.resources.spaces_empty_body
import com.project.whobusy.resources.spaces_empty_footer
import com.project.whobusy.resources.spaces_empty_title
import com.project.whobusy.resources.spaces_invitation_count
import com.project.whobusy.resources.spaces_invitations
import com.project.whobusy.resources.spaces_join_key
import com.project.whobusy.resources.spaces_member_role
import com.project.whobusy.resources.spaces_my_spaces
import com.project.whobusy.resources.spaces_roles_local
import com.project.whobusy.resources.spaces_title
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SpacesScreen(
    state: SpacesState,
    onOpenSpace: (Int) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onInvitations: () -> Unit,
) {
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(Res.string.spaces_title),
                action = {
                    WhoBusyIconButton(UiIcon.Add, stringResource(Res.string.spaces_create), onCreate, tint = Color.White)
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("spaces-content"),
            contentPadding = PaddingValues(bottom = 110.dp),
        ) {
            item(key = "feedback") {
                SpacesFeedback(state.isLoading, state.error)
            }
            if (state.spaces.isEmpty() && !state.isLoading) {
                if (state.invitations.isNotEmpty()) {
                    item(key = "invitations") {
                        WhoBusyListRow(
                            stringResource(Res.string.spaces_invitations),
                            stringResource(Res.string.spaces_invitation_count, state.invitations.size),
                            onInvitations,
                        )
                    }
                }
                item(key = "empty") {
                    EmptySpaces(onCreate, onJoin)
                }
            } else {
                item(key = "header") {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        WhoBusyListRow(
                            stringResource(Res.string.spaces_invitations),
                            stringResource(Res.string.spaces_invitation_count, state.invitations.size),
                            onInvitations,
                            trailing = { WhoBusyIcon(UiIcon.Mail, tint = MaterialTheme.colorScheme.primary) },
                        )
                        Spacer(Modifier.height(24.dp))
                        WhoBusySectionTitle(
                            stringResource(Res.string.spaces_my_spaces),
                            Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
                items(state.spaces, key = { "space:${it.id}" }, contentType = { "space" }) { space ->
                    WhoBusyListRow(
                        title = space.name,
                        subtitle =
                            stringResource(
                                Res.string.spaces_member_role,
                                space.memberCount,
                                spaceRoleTitle(space.myRole),
                            ),
                        onClick = { onOpenSpace(space.id) },
                        leading = { WhoBusyAvatar(space.name.take(1), spaceColor(space.id)) },
                    )
                }
                item(key = "actions") {
                    Column {
                        Spacer(Modifier.height(48.dp))
                        WhoBusyButton(
                            stringResource(Res.string.spaces_create),
                            onCreate,
                            Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        WhoBusyButton(
                            stringResource(Res.string.spaces_join_key),
                            onJoin,
                            Modifier.padding(horizontal = 16.dp),
                            style = ButtonStyle.Secondary,
                        )
                        Text(
                            stringResource(Res.string.spaces_roles_local),
                            Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySpaces(
    onCreate: () -> Unit,
    onJoin: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(108.dp))
        androidx.compose.material3.Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape =
                androidx.compose.foundation.shape
                    .RoundedCornerShape(28.dp),
        ) {
            androidx.compose.foundation.layout.Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                WhoBusyIcon(UiIcon.People, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(48.dp))
        Text(
            stringResource(Res.string.spaces_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.spaces_empty_body),
            Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        WhoBusyButton(stringResource(Res.string.spaces_create), onCreate)
        Spacer(Modifier.height(16.dp))
        WhoBusyButton(stringResource(Res.string.spaces_join_key), onJoin, style = ButtonStyle.Secondary)
        Text(
            stringResource(Res.string.spaces_empty_footer),
            Modifier.padding(top = 72.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SpacesPreview() {
    WhoBusyTheme { SpacesScreen(SpacesPreviewData.state, {}, {}, {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EmptySpacesPreview() {
    WhoBusyTheme { SpacesScreen(SpacesState(), {}, {}, {}, {}) }
}
