package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySelectionMark
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_assign_admin
import com.project.whobusy.resources.spaces_cancel
import com.project.whobusy.resources.spaces_delete
import com.project.whobusy.resources.spaces_last_admin_body
import com.project.whobusy.resources.spaces_last_admin_title
import com.project.whobusy.resources.spaces_leave
import com.project.whobusy.resources.spaces_leave_named
import com.project.whobusy.resources.spaces_leave_plans_hint
import com.project.whobusy.resources.spaces_no_successor
import com.project.whobusy.resources.spaces_stay
import com.project.whobusy.resources.spaces_successor_subtitle
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LeaveSpaceScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    var selectingSuccessor by rememberSaveable { mutableStateOf(false) }
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(stringResource(Res.string.spaces_leave_named, state.space?.name.orEmpty()), onBack)
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            SpacesFeedback(state.isLoading || state.isSaving, state.error, state.message)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(36.dp))
                if (state.isLastAdmin) {
                    WhoBusyNotice(
                        stringResource(Res.string.spaces_last_admin_title),
                        stringResource(Res.string.spaces_last_admin_body),
                        Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(48.dp))
                    state.successors.firstOrNull { it.userId == state.successorId }?.let { member ->
                        WhoBusyListRow(
                            member.fullName,
                            stringResource(Res.string.spaces_successor_subtitle, spaceRoleTitle(member.role)),
                            onClick = { if (!state.isSaving) selectingSuccessor = true },
                            leading = { WhoBusyAvatar(member.fullName, calendarColor(member.color)) },
                        )
                    }
                    if (state.successors.isEmpty()) {
                        Text(
                            stringResource(Res.string.spaces_no_successor),
                            Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                    WhoBusyButton(
                        stringResource(Res.string.spaces_assign_admin),
                        {
                            onEvent(SpacesCommand.TransferAdmin)
                        },
                        Modifier.padding(horizontal = 16.dp),
                        enabled =
                            state.successorId != null && !state.isSaving,
                    )
                } else {
                    WhoBusyButton(
                        stringResource(Res.string.spaces_leave),
                        {
                            onEvent(SpacesCommand.Leave)
                        },
                        Modifier.padding(horizontal = 16.dp),
                        style = ButtonStyle.Destructive,
                        enabled =
                            state.space != null && !state.isSaving,
                    )
                }
                Text(
                    stringResource(Res.string.spaces_leave_plans_hint),
                    Modifier.padding(horizontal = 20.dp, vertical = 56.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 24.dp)) {
                WhoBusyButton(
                    stringResource(Res.string.spaces_stay),
                    onBack,
                    style = ButtonStyle.Secondary,
                    enabled = !state.isSaving,
                )
                if (state.space?.myRole == SpaceRole.Admin) {
                    Spacer(Modifier.height(16.dp))
                    WhoBusyButton(
                        stringResource(Res.string.spaces_delete),
                        onSettings,
                        style = ButtonStyle.Destructive,
                        enabled = !state.isSaving,
                    )
                }
            }
        }
    }
    if (selectingSuccessor) {
        AlertDialog(
            onDismissRequest = { selectingSuccessor = false },
            title = { Text(stringResource(Res.string.spaces_assign_admin)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 320.dp).selectableGroup()) {
                    items(state.successors, key = { "successor:${it.userId}" }) { member ->
                        WhoBusyListRow(
                            member.fullName,
                            spaceRoleTitle(member.role),
                            leading = { WhoBusyAvatar(member.fullName, calendarColor(member.color)) },
                            trailing = { WhoBusySelectionMark(state.successorId == member.userId) },
                            modifier =
                                Modifier.selectable(
                                    state.successorId == member.userId,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onEvent(SpacesCommand.SuccessorSelected(member.userId))
                                        selectingSuccessor = false
                                    },
                                ),
                        )
                    }
                }
            },
            confirmButton = {
                WhoBusyTextButton(stringResource(Res.string.spaces_cancel), { selectingSuccessor = false })
            },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LastAdminLeavePreview() {
    WhoBusyTheme {
        LeaveSpaceScreen(
            SpacesPreviewData.state.copy(members = SpacesPreviewData.members.take(2)),
            {},
            {},
            {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LeaveSpacePreview() {
    WhoBusyTheme { LeaveSpaceScreen(SpacesPreviewData.state.copy(spaceId = 3), {}, {}, {}) }
}
