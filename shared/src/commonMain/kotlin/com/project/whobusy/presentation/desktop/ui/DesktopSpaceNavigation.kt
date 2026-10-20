package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.spaces.ui.SpacesPreviewData
import com.project.whobusy.presentation.spaces.ui.spaceColor
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.desktop_space_people
import com.project.whobusy.resources.desktop_space_people_body
import com.project.whobusy.resources.desktop_space_tagline
import com.project.whobusy.resources.spaces_create
import com.project.whobusy.resources.spaces_invitations
import com.project.whobusy.resources.spaces_join_key
import com.project.whobusy.resources.spaces_my_spaces
import com.project.whobusy.resources.spaces_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopSpaceNavigation(
    spaces: List<Space>,
    selectedSpaceId: Int?,
    invitationCount: Int,
    onSpace: (Int) -> Unit,
    onCreateSpace: () -> Unit,
    onJoinSpace: () -> Unit,
    onInvitations: () -> Unit,
    enabled: Boolean = true,
) {
    Column(Modifier.width(280.dp).fillMaxHeight().background(Color.White)) {
        Row(
            Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.spaces_title), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            WhoBusyIconButton(UiIcon.Add, stringResource(Res.string.spaces_create), onCreateSpace, enabled = enabled)
        }
        LazyColumn(Modifier.weight(1f)) {
            item(key = "actions") {
                Column(
                    Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WhoBusyButton(stringResource(Res.string.spaces_create), onCreateSpace, enabled = enabled)
                    WhoBusyButton(
                        stringResource(Res.string.spaces_join_key),
                        onJoinSpace,
                        style = ButtonStyle.Secondary,
                        enabled = enabled,
                    )
                }
            }
            item(key = "title") {
                Text(
                    stringResource(Res.string.spaces_my_spaces),
                    Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = WhoBusyColors.SecondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            items(spaces, key = { it.id }) { space ->
                Surface(
                    onClick = { onSpace(space.id) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
                    color = if (space.id == selectedSpaceId) WhoBusyColors.BlueTint else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    enabled = enabled,
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WhoBusyAvatar(space.name, spaceColor(space.id), size = 44.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(space.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                spaceRoleTitle(space.myRole),
                                color = if (space.id == selectedSpaceId) WhoBusyColors.Primary else WhoBusyColors.SecondaryText,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item(key = "invitations") {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    HorizontalDivider(color = WhoBusyColors.Divider)
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onInvitations).padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WhoBusyIcon(UiIcon.Mail, tint = WhoBusyColors.Primary)
                        Text(stringResource(Res.string.spaces_invitations), Modifier.weight(1f))
                        if (invitationCount > 0) {
                            Surface(shape = RoundedCornerShape(20.dp), color = WhoBusyColors.BlueTint) {
                                Text(
                                    invitationCount.toString(),
                                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = WhoBusyColors.Primary,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
            }
            item(key = "notice") {
                WhoBusyNotice(
                    stringResource(Res.string.desktop_space_people),
                    stringResource(Res.string.desktop_space_people_body),
                    Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                )
            }
            item(key = "footer") {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("WhoBusy", color = WhoBusyColors.SecondaryText, fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(Res.string.desktop_space_tagline),
                        color = WhoBusyColors.SecondaryText,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 280, heightDp = 960)
@Composable
private fun DesktopSpaceNavigationPreview() {
    WhoBusyTheme { DesktopSpaceNavigation(SpacesPreviewData.spaces, 1, 2, {}, {}, {}, {}) }
}

@Preview(name = "Низкое окно", widthDp = 280, heightDp = 480)
@Composable
private fun DesktopShortSpaceNavigationPreview() {
    WhoBusyTheme { DesktopSpaceNavigation(SpacesPreviewData.spaces, 1, 2, {}, {}, {}, {}) }
}
