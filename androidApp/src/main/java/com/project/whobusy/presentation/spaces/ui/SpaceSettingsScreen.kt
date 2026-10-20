package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState

@Composable
fun SpaceSettingsScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
    onLeave: () -> Unit,
    initialDeleteDialog: Boolean = false,
) {
    var deleting by rememberSaveable { mutableStateOf(initialDeleteDialog) }
    val canEdit = state.space?.myRole == SpaceRole.Admin
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.spaces_settings), onBack) }) {
        Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
            SpacesFeedback(state.isLoading || state.isSaving, state.error, state.message)
            if (canEdit) {
                Spacer(Modifier.height(24.dp))
                WhoBusyTextField(
                    state.name,
                    {
                        onEvent(SpacesCommand.NameChanged(it))
                    },
                    stringResource(
                        R.string.spaces_name,
                    ),
                    Modifier.padding(horizontal = 16.dp),
                    enabled = !state.isSaving,
                )
                Spacer(Modifier.height(22.dp))
                WhoBusyButton(
                    stringResource(R.string.spaces_save_name),
                    {
                        onEvent(SpacesCommand.Rename)
                    },
                    Modifier.padding(horizontal = 16.dp), enabled = !state.isSaving
                )
                Spacer(Modifier.height(28.dp))
            }
            WhoBusySectionTitle(
                stringResource(R.string.spaces_membership_section),
                Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            )
            WhoBusyListRow(
                stringResource(
                    R.string.spaces_leave,
                ),
                stringResource(R.string.spaces_leave_hint),
                onClick = {
                    if (!state.isSaving) onLeave()
                },
            )
            if (canEdit) {
                Spacer(Modifier.height(28.dp))
                WhoBusySectionTitle(
                    stringResource(R.string.spaces_delete_section),
                    Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                )
                WhoBusyNotice(
                    stringResource(R.string.spaces_delete_notice_title),
                    stringResource(R.string.spaces_delete_notice_body),
                    Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(32.dp))
                WhoBusyButton(
                    stringResource(R.string.spaces_delete),
                    {
                        deleting = true
                    },
                    Modifier.padding(horizontal = 16.dp), style = ButtonStyle.Destructive, enabled = !state.isSaving
                )
            }
            Spacer(Modifier.height(48.dp))
        }
    }
    if (deleting && canEdit) {
        DeleteSpaceDialog(
            state.space?.name.orEmpty(), onDismiss = { deleting = false },
            onDelete = {
                deleting = false
                onEvent(SpacesCommand.Delete)
            }
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SpaceSettingsPreview() {
    WhoBusyTheme { SpaceSettingsScreen(SpacesPreviewData.state, {}, {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DeleteSpacePreview() {
    WhoBusyTheme { SpaceSettingsScreen(SpacesPreviewData.state, {}, {}, {}, initialDeleteDialog = true) }
}
