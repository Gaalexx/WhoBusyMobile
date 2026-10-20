package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusySelectionMark
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyToggle
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.presentation.calendar.ui.statusTitle
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorState

@Composable
fun EventEditorScreen(
    state: EditorState,
    onEvent: (EditorCommand) -> Unit,
    onBack: () -> Unit,
    onVisibility: () -> Unit,
    onDates: () -> Unit,
) {
    var placement by remember { mutableStateOf(false) }
    val form = state.form
    val space = state.spaces.firstOrNull { it.id == form.spaceId }
    val saveLabel =
        when {
            state.eventId != null -> stringResource(R.string.plan_save)
            space != null -> stringResource(R.string.plan_create_in, space.name)
            else -> stringResource(R.string.plan_create)
        }
    EventPage(
        title = stringResource(if (state.eventId == null) R.string.plan_new else R.string.plan_edit),
        onBack = { if (!state.isSaving) onBack() },
        action = {
            if (space == null) {
                WhoBusyIconButton(
                    UiIcon.Check,
                    saveLabel,
                    { onEvent(EditorCommand.Save) },
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        footer = {
            WhoBusyButton(
                saveLabel,
                { onEvent(EditorCommand.Save) },
                Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                loading = state.isSaving,
                enabled = !state.isLoading,
            )
        },
    ) {
        item {
            Column {
                Spacer(Modifier.height(16.dp))
                WhoBusyTextField(
                    form.title,
                    { onEvent(EditorCommand.Title(it)) },
                    stringResource(R.string.event_title),
                    Modifier.padding(horizontal = 16.dp),
                    enabled = !state.isSaving,
                )
                Spacer(Modifier.height(8.dp))
                WhoBusyListRow(
                    stringResource(R.string.plan_placement),
                    if (space ==
                        null
                    ) {
                        stringResource(R.string.plan_personal)
                    } else {
                        stringResource(R.string.plan_in_space, space.name)
                    },
                    if (state.eventId == null) ({ placement = true }) else null,
                )
                if (space != null) {
                    WhoBusyNotice(
                        stringResource(R.string.plan_visible_everyone, space.name),
                        stringResource(R.string.plan_visible_everyone_body),
                        Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                WhoBusyListRow(stringResource(R.string.plan_date_range), planDates(form), onDates)
                if (space == null) {
                    Spacer(Modifier.height(8.dp))
                    WhoBusyListRow(
                        stringResource(R.string.label_all_day),
                        stringResource(R.string.plan_all_day_hint),
                        trailing = { WhoBusyToggle(form.allDay, { onEvent(EditorCommand.AllDay(it)) }) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                WhoBusyListRow(
                    stringResource(R.string.plan_repeat_status),
                    stringResource(
                        R.string.plan_repeat_status_value,
                        recurrenceTitle(form.recurrenceRule),
                        statusTitle(form.status),
                    ),
                    onDates,
                )
                Spacer(Modifier.height(10.dp))
                WhoBusyTextField(
                    form.location,
                    { onEvent(EditorCommand.Location(it)) },
                    stringResource(R.string.event_location),
                    Modifier.padding(horizontal = 16.dp),
                    enabled = !state.isSaving,
                )
                Spacer(Modifier.height(12.dp))
                WhoBusyTextField(
                    form.description,
                    { onEvent(EditorCommand.Description(it)) },
                    stringResource(R.string.event_description),
                    Modifier.padding(horizontal = 16.dp),
                    enabled = !state.isSaving,
                    singleLine = false,
                )
                if (space == null && state.canChangeVisibility) {
                    Spacer(Modifier.height(12.dp))
                    val subtitle =
                        when {
                            form.shares.isEmpty() -> stringResource(R.string.plan_visibility_private)
                            form.shares.all {
                                it.visibility == EventVisibility.Busy
                            } -> stringResource(R.string.plan_visibility_busy_count, form.shares.size)

                            else -> stringResource(R.string.plan_visibility_count, form.shares.size)
                        }
                    WhoBusyListRow(stringResource(R.string.plan_visibility), subtitle, onVisibility)
                }
                (form.validationError ?: state.error)?.let {
                    Text(stringResource(it), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    if (placement) {
        AlertDialog(
            onDismissRequest = { placement = false },
            title = { Text(stringResource(R.string.plan_placement)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    item(key = "personal") {
                        WhoBusyListRow(
                            stringResource(R.string.plan_personal),
                            onClick = {
                                onEvent(EditorCommand.Placement(null))
                                placement = false
                            },
                            trailing = { WhoBusySelectionMark(form.spaceId == null) },
                        )
                    }
                    items(state.spaces, key = { it.id }) { item ->
                        WhoBusyListRow(
                            item.name,
                            onClick = {
                                onEvent(EditorCommand.Placement(item.id))
                                placement = false
                            },
                            trailing = { WhoBusySelectionMark(form.spaceId == item.id) },
                        )
                    }
                }
            },
            confirmButton = { WhoBusyTextButton(stringResource(R.string.action_cancel), { placement = false }) },
        )
    }
}

@Preview(name = "Личный план", widthDp = 390, heightDp = 844)
@Composable
private fun PersonalPlanEditorPreview() {
    WhoBusyTheme { EventEditorScreen(EventPreviewData.editor, {}, {}, {}, {}) }
}

@Preview(name = "В пространстве", widthDp = 390, heightDp = 844)
@Composable
private fun SpacePlanEditorPreview() {
    WhoBusyTheme {
        EventEditorScreen(
            EventPreviewData.editor.copy(
                form = EventPreviewData.editor.form.copy(spaceId = 1, shares = emptyList()),
            ),
            {
            },
            {},
            {},
            {},
        )
    }
}
