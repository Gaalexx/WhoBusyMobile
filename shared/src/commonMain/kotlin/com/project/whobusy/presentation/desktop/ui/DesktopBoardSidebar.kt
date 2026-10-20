package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.desktop_all_spaces
import com.project.whobusy.resources.desktop_board_hint
import com.project.whobusy.resources.desktop_board_hint_body
import com.project.whobusy.resources.desktop_brand_caption
import com.project.whobusy.resources.desktop_create_space
import com.project.whobusy.resources.desktop_join_space
import com.project.whobusy.resources.desktop_my_board
import com.project.whobusy.resources.desktop_no_spaces
import com.project.whobusy.resources.desktop_only_own
import com.project.whobusy.resources.desktop_own_plans
import com.project.whobusy.resources.desktop_shared
import com.project.whobusy.resources.desktop_show
import com.project.whobusy.resources.desktop_space_members
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopSpaceSidebar(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
    onOpenSpace: (Int) -> Unit,
    onSpaces: () -> Unit,
    onCreateSpace: () -> Unit,
    onJoinSpace: () -> Unit,
) {
    val enabled = !state.isLoading && !state.isSaving
    val update = { settings: BoardSettings -> onEvent(CalendarCommand.UpdateBoardSettings(settings)) }
    Column(Modifier.width(280.dp).fillMaxHeight().background(Color.White)) {
        LazyColumn(Modifier.weight(1f).padding(horizontal = 20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().height(80.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.desktop_my_board), Modifier.weight(1f), fontSize = 20.sp)
                    IconButton(onCreateSpace) {
                        WhoBusyIcon(UiIcon.Add, stringResource(Res.string.desktop_create_space), tint = DesktopPalette.Accent)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DesktopSidebarTab(
                        stringResource(Res.string.desktop_shared), state.boardSettings.spaceIds.isNotEmpty(), enabled,
                        Modifier.weight(1f),
                    ) { update(BoardSettings(state.spaces.map { it.id }.toSet())) }
                    DesktopSidebarTab(
                        stringResource(Res.string.desktop_only_own), state.boardSettings.spaceIds.isEmpty(), enabled,
                        Modifier.weight(1f),
                    ) { update(BoardSettings(emptySet())) }
                }
                Spacer(Modifier.height(28.dp))
                Text(stringResource(Res.string.desktop_show), fontSize = 12.sp, color = DesktopPalette.Muted)
                Row(Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.desktop_own_plans), Modifier.weight(1f), fontSize = 16.sp)
                    Checkbox(
                        state.boardSettings.showOwn,
                        { update(state.boardSettings.copy(showOwn = it)) }, enabled = enabled,
                    )
                }
            }
            items(state.spaces, key = { it.id }, contentType = { "desktop-space-filter" }) { space ->
                Row(Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.weight(1f).clickable { onOpenSpace(space.id) },
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WhoBusyAvatar(space.name.take(1), DesktopPalette.Accent, 32.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(space.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                stringResource(Res.string.desktop_space_members, space.memberCount, spaceRoleTitle(space.myRole)),
                                fontSize = 11.sp, color = DesktopPalette.Muted, maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Checkbox(
                        space.id in state.boardSettings.spaceIds,
                        {
                            val selected = state.boardSettings.spaceIds
                            update(state.boardSettings.copy(spaceIds = if (it) selected + space.id else selected - space.id))
                        }, enabled = enabled,
                    )
                }
            }
            if (state.spaces.isEmpty() && !state.isLoading) {
                item { Text(stringResource(Res.string.desktop_no_spaces), fontSize = 13.sp, color = DesktopPalette.Muted) }
            }
            item {
                TextButton(onSpaces) { Text(stringResource(Res.string.desktop_all_spaces), fontSize = 12.sp) }
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = DesktopPalette.Border)
                DesktopMiniCalendar(state.selectedDate) { onEvent(CalendarCommand.SelectDate(it)) }
                Spacer(Modifier.height(30.dp))
                Column(
                    Modifier.fillMaxWidth().background(DesktopPalette.Selection, RoundedCornerShape(12.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(stringResource(Res.string.desktop_board_hint), color = DesktopPalette.Accent, fontSize = 14.sp)
                    Text(stringResource(Res.string.desktop_board_hint_body), color = DesktopPalette.Accent, fontSize = 13.sp)
                }
                TextButton(onJoinSpace) { Text(stringResource(Res.string.desktop_join_space), fontSize = 12.sp) }
            }
        }
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("WhoBusy", color = DesktopPalette.Muted, fontSize = 14.sp)
            Text(stringResource(Res.string.desktop_brand_caption), color = DesktopPalette.Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DesktopSidebarTab(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick, modifier.height(32.dp).background(
            if (selected) DesktopPalette.Selection else DesktopPalette.Background, RoundedCornerShape(18.dp),
        ), enabled = enabled,
    ) {
        Text(text, fontSize = 12.sp, color = if (selected) DesktopPalette.Accent else DesktopPalette.Muted)
    }
}

@Preview(widthDp = 280, heightDp = 960)
@Composable
private fun DesktopSpaceSidebarPreview() {
    WhoBusyTheme { DesktopSpaceSidebar(CalendarPreviewData.state, {}, {}, {}, {}, {}) }
}

@Preview(name = "Нет пространств", widthDp = 280, heightDp = 960)
@Composable
private fun DesktopEmptySidebarPreview() {
    WhoBusyTheme { DesktopSpaceSidebar(CalendarPreviewData.state.copy(spaces = emptyList()), {}, {}, {}, {}, {}) }
}
