package com.project.whobusy.presentation.board.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusySelectionMark
import com.project.whobusy.core.ui.components.WhoBusyToggle
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsCommand
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsState
import com.project.whobusy.presentation.spaces.ui.SpacesFeedback
import com.project.whobusy.presentation.spaces.ui.SpacesPreviewData
import com.project.whobusy.presentation.spaces.ui.spaceColor
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle

@Composable
fun BoardSettingsScreen(
    state: BoardSettingsState,
    onEvent: (BoardSettingsCommand) -> Unit,
    onBack: () -> Unit,
) {
    val enabled = !state.isLoading && !state.isSaving
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(R.string.board_settings_title), onBack,
                action = {
                    if (enabled) {
                        WhoBusyIconButton(
                            UiIcon.Check, stringResource(R.string.board_show),
                            {
                                onEvent(BoardSettingsCommand.Save)
                            },
                            tint = Color.White,
                        )
                    }
                },
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            SpacesFeedback(state.isLoading, state.error)
            LazyColumn(Modifier.weight(1f)) {
                item(key = "quick-filters", contentType = "quick-filters") {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            onClick = {
                                if (enabled) onEvent(BoardSettingsCommand.AllSpaces)
                            },
                            shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                stringResource(R.string.board_shared),
                                Modifier.padding(horizontal = 36.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Surface(
                            onClick = {
                                if (enabled) onEvent(BoardSettingsCommand.OnlyOwn)
                            },
                            shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                stringResource(R.string.board_only_own),
                                Modifier.padding(horizontal = 26.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                item(key = "own-plans", contentType = "own-plans") {
                    WhoBusyListRow(
                        stringResource(R.string.board_my_plans),
                        stringResource(R.string.board_my_plans_body),
                        trailing = {
                            WhoBusyToggle(
                                state.settings.showOwn,
                                { if (enabled) onEvent(BoardSettingsCommand.ShowOwn(it)) },
                            )
                        },
                        modifier = Modifier.toggleable(
                            state.settings.showOwn, enabled = enabled, role = Role.Switch,
                            onValueChange = {
                                onEvent(BoardSettingsCommand.ShowOwn(it))
                            },
                        ),
                    )
                }
                item(key = "spaces-heading", contentType = "section-heading") {
                    Spacer(Modifier.height(24.dp))
                    WhoBusySectionTitle(
                        stringResource(R.string.board_add_spaces),
                        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                }
                items(state.spaces, key = { it.id }, contentType = { "space" }) { space ->
                    val selected = space.id in state.settings.spaceIds
                    WhoBusyListRow(
                        space.name,
                        stringResource(
                            R.string.board_space_role_count,
                            spaceRoleTitle(space.myRole),
                            space.memberCount,
                        ),
                        trailing = { WhoBusySelectionMark(selected) },
                        leading = { WhoBusyAvatar(space.name.take(1), spaceColor(space.id)) },
                        modifier = Modifier.toggleable(
                            selected, enabled = enabled, role = Role.Checkbox,
                            onValueChange = {
                                onEvent(BoardSettingsCommand.ToggleSpace(space.id))
                            },
                        ),
                    )
                }
                item(key = "notice", contentType = "notice") {
                    WhoBusyNotice(
                        stringResource(R.string.board_no_duplicates_title),
                        stringResource(R.string.board_no_duplicates_body),
                        Modifier.padding(horizontal = 16.dp, vertical = 38.dp),
                    )
                }
            }
            WhoBusyButton(
                stringResource(R.string.board_show),
                {
                    onEvent(BoardSettingsCommand.Save)
                },
                Modifier
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 24.dp,
                    ),
                enabled = !state.isLoading,
                loading = state.isSaving,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun BoardSettingsPreview() {
    WhoBusyTheme {
        BoardSettingsScreen(
            BoardSettingsState(SpacesPreviewData.spaces, BoardSettings(setOf(1, 2))),
            {},
            {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun OwnBoardPreview() {
    WhoBusyTheme {
        BoardSettingsScreen(
            BoardSettingsState(SpacesPreviewData.spaces, BoardSettings(emptySet())),
            {},
            {},
        )
    }
}
