package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.spaces.ui.SpacesFeedback
import com.project.whobusy.presentation.spaces.ui.SpacesPreviewData
import com.project.whobusy.presentation.spaces.ui.spaceColor
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.desktop_space_choose
import com.project.whobusy.resources.desktop_space_choose_body
import com.project.whobusy.resources.spaces_create
import com.project.whobusy.resources.spaces_empty_body
import com.project.whobusy.resources.spaces_empty_title
import com.project.whobusy.resources.spaces_error_load
import com.project.whobusy.resources.spaces_invitation_count
import com.project.whobusy.resources.spaces_invitations
import com.project.whobusy.resources.spaces_join_key
import com.project.whobusy.resources.spaces_member_role
import com.project.whobusy.resources.spaces_retry
import com.project.whobusy.resources.spaces_roles_local
import com.project.whobusy.resources.spaces_title
import com.project.whobusy.resources.ui_loading
import org.jetbrains.compose.resources.stringResource

@Composable
fun DesktopSpacesScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onOpenSpace: (Int) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onInvitations: () -> Unit,
) {
    val navigationEnabled = !state.isSaving && state.result == null
    Row(Modifier.fillMaxSize().testTag("desktop-spaces")) {
        DesktopSpaceNavigation(
            spaces = state.spaces,
            selectedSpaceId = null,
            invitationCount = state.invitations.size,
            onSpace = onOpenSpace,
            onCreateSpace = onCreate,
            onJoinSpace = onJoin,
            onInvitations = onInvitations,
            enabled = navigationEnabled,
        )
        Column(Modifier.weight(1f).fillMaxHeight().background(WhoBusyColors.Background)) {
            Column(
                Modifier.fillMaxWidth().height(80.dp).background(WhoBusyColors.Header).padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(Res.string.spaces_title), color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            SpacesFeedback(state.isLoading, state.error, state.message)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item(key = "summary") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(Res.string.desktop_space_choose),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            stringResource(Res.string.desktop_space_choose_body),
                            color = WhoBusyColors.SecondaryText,
                        )
                    }
                }
                if (state.isLoading && state.spaces.isEmpty()) {
                    item(key = "loading") {
                        Text(stringResource(Res.string.ui_loading), color = WhoBusyColors.SecondaryText)
                    }
                }
                if (state.error != null) {
                    item(key = "retry") {
                        WhoBusyButton(
                            stringResource(Res.string.spaces_retry),
                            { onEvent(SpacesCommand.Load()) },
                            Modifier.width(180.dp),
                            enabled = !state.isLoading && navigationEnabled,
                        )
                    }
                }
                if (state.invitations.isNotEmpty()) {
                    item(key = "invitations") {
                        Surface(
                            onClick = onInvitations,
                            enabled = navigationEnabled,
                            shape = RoundedCornerShape(14.dp),
                            color = WhoBusyColors.BlueTint,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                WhoBusyIcon(UiIcon.Mail, tint = WhoBusyColors.Primary)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(stringResource(Res.string.spaces_invitations))
                                    Text(
                                        stringResource(Res.string.spaces_invitation_count, state.invitations.size),
                                        color = WhoBusyColors.Primary,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                WhoBusyIcon(UiIcon.Next)
                            }
                        }
                    }
                }
                if (state.spaces.isEmpty() && !state.isLoading && state.error == null) {
                    item(key = "empty") {
                        Surface(color = Color.White, shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                WhoBusyIcon(UiIcon.People, tint = WhoBusyColors.Primary)
                                Text(stringResource(Res.string.spaces_empty_title), style = MaterialTheme.typography.titleLarge)
                                Text(stringResource(Res.string.spaces_empty_body), color = WhoBusyColors.SecondaryText)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    WhoBusyButton(
                                        stringResource(Res.string.spaces_create),
                                        onCreate,
                                        Modifier.weight(1f),
                                        enabled = navigationEnabled,
                                    )
                                    WhoBusyButton(
                                        stringResource(Res.string.spaces_join_key),
                                        onJoin,
                                        Modifier.weight(1f),
                                        style = ButtonStyle.Secondary,
                                        enabled = navigationEnabled,
                                    )
                                }
                            }
                        }
                    }
                }
                items(state.spaces, key = { it.id }, contentType = { "desktop-space-card" }) { space ->
                    Surface(
                        onClick = { onOpenSpace(space.id) },
                        enabled = navigationEnabled,
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            WhoBusyAvatar(space.name, spaceColor(space.id), size = 56.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(space.name, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    stringResource(Res.string.spaces_member_role, space.memberCount, spaceRoleTitle(space.myRole)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WhoBusyColors.SecondaryText,
                                )
                            }
                            WhoBusyIcon(UiIcon.Next)
                        }
                    }
                }
                item(key = "roles") {
                    Text(stringResource(Res.string.spaces_roles_local), color = WhoBusyColors.SecondaryText)
                }
            }
        }
    }
}

@Preview(widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopSpacesPreview() {
    WhoBusyTheme { DesktopSpacesScreen(SpacesPreviewData.state, {}, {}, {}, {}, {}) }
}

@Preview(name = "Нет пространств", widthDp = 1360, heightDp = 600)
@Composable
private fun DesktopEmptySpacesPreview() {
    WhoBusyTheme { DesktopSpacesScreen(SpacesState(), {}, {}, {}, {}, {}) }
}

@Preview(name = "Ошибка загрузки", widthDp = 1360, heightDp = 600)
@Composable
private fun DesktopSpacesErrorPreview() {
    WhoBusyTheme { DesktopSpacesScreen(SpacesState(error = Res.string.spaces_error_load), {}, {}, {}, {}, {}) }
}

@Preview(name = "Загрузка", widthDp = 1360, heightDp = 600)
@Composable
private fun DesktopSpacesLoadingPreview() {
    WhoBusyTheme { DesktopSpacesScreen(SpacesState(isLoading = true), {}, {}, {}, {}, {}) }
}
