package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyValueButton
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.ui.SpacesPreviewData
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.desktop_space_access_notice
import com.project.whobusy.resources.desktop_space_access_notice_body
import com.project.whobusy.resources.spaces_copy_key
import com.project.whobusy.resources.spaces_invite
import com.project.whobusy.resources.spaces_key_hint
import com.project.whobusy.resources.spaces_key_label
import com.project.whobusy.resources.spaces_key_section
import com.project.whobusy.resources.spaces_login
import com.project.whobusy.resources.spaces_login_section
import com.project.whobusy.resources.spaces_regenerate_key
import com.project.whobusy.resources.spaces_role_after_accept
import com.project.whobusy.resources.spaces_send_invitation
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopSpaceInvitePanel(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectingRole by remember { mutableStateOf(false) }
    val enabled = !state.isLoading && !state.isSaving && state.result == null
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Color.White) {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "title") {
                Text(stringResource(Res.string.spaces_invite), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
            }
            item(key = "key") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    WhoBusySectionTitle(stringResource(Res.string.spaces_key_section))
                    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                        WhoBusyValueButton(
                            stringResource(Res.string.spaces_key_label),
                            state.space?.inviteCode.orEmpty(),
                            { if (enabled) onEvent(SpacesCommand.CopyCode) },
                            enabled = enabled && !state.space?.inviteCode.isNullOrEmpty(),
                        )
                    }
                    WhoBusyButton(
                        stringResource(Res.string.spaces_copy_key),
                        { onEvent(SpacesCommand.CopyCode) },
                        style = ButtonStyle.Secondary,
                        enabled = enabled && !state.space?.inviteCode.isNullOrEmpty(),
                    )
                    WhoBusyTextButton(
                        stringResource(Res.string.spaces_regenerate_key),
                        { onEvent(SpacesCommand.RegenerateCode) },
                        enabled = enabled,
                    )
                    Text(
                        stringResource(Res.string.spaces_key_hint),
                        color = WhoBusyColors.SecondaryText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = WhoBusyColors.Divider)
                }
            }
            item(key = "form") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    WhoBusySectionTitle(stringResource(Res.string.spaces_login_section))
                    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                        WhoBusyTextField(
                            state.login,
                            { onEvent(SpacesCommand.LoginChanged(it)) },
                            stringResource(Res.string.spaces_login),
                            enabled = enabled,
                        )
                        Box {
                            WhoBusyValueButton(
                                stringResource(Res.string.spaces_role_after_accept),
                                spaceRoleTitle(state.selectedRole),
                                { if (enabled) selectingRole = true },
                                enabled = enabled,
                                trailingIcon = UiIcon.ExpandMore,
                            )
                            DropdownMenu(selectingRole, { selectingRole = false }) {
                                SpaceRole.entries.filter {
                                    state.space?.myRole == SpaceRole.Admin || it != SpaceRole.Admin
                                }.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(spaceRoleTitle(role)) },
                                        onClick = {
                                            onEvent(SpacesCommand.RoleChanged(role))
                                            selectingRole = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                    WhoBusyButton(
                        stringResource(Res.string.spaces_send_invitation),
                        { onEvent(SpacesCommand.Invite) },
                        enabled = enabled && state.login.isNotBlank(),
                        loading = state.isSaving,
                    )
                    WhoBusyNotice(
                        stringResource(Res.string.desktop_space_access_notice),
                        stringResource(Res.string.desktop_space_access_notice_body),
                        Modifier.padding(top = 24.dp),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 312, heightDp = 832)
@Composable
private fun DesktopSpaceInvitePreview() {
    WhoBusyTheme { DesktopSpaceInvitePanel(SpacesPreviewData.state.copy(login = "olga"), {}) }
}
