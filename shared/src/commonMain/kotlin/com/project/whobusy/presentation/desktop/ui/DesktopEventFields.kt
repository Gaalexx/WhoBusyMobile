package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyChoicePill
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyToggle
import com.project.whobusy.core.ui.components.WhoBusyValueButton
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.calendar.ui.statusTitle
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.ui.EventDateTimePicker
import com.project.whobusy.presentation.event.ui.recurrenceOptions
import com.project.whobusy.presentation.event.ui.recurrenceTitle
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.desktop_editor_details
import com.project.whobusy.resources.desktop_editor_in_space
import com.project.whobusy.resources.desktop_editor_placement
import com.project.whobusy.resources.desktop_editor_series_notice
import com.project.whobusy.resources.event_description
import com.project.whobusy.resources.event_end
import com.project.whobusy.resources.event_location
import com.project.whobusy.resources.event_start
import com.project.whobusy.resources.event_status
import com.project.whobusy.resources.event_title
import com.project.whobusy.resources.label_all_day
import com.project.whobusy.resources.plan_all_day_hint
import com.project.whobusy.resources.plan_last_day
import com.project.whobusy.resources.plan_personal
import com.project.whobusy.resources.plan_placement
import com.project.whobusy.resources.plan_repeat_heading
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.format
import com.project.whobusy.time.minusDays
import com.project.whobusy.time.parse
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.toLocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopEventFields(
    state: EditorState,
    onEvent: (EditorCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
    var choosingSpace by remember { mutableStateOf(false) }
    var choosingRecurrence by remember { mutableStateOf(false) }
    var choosingStatus by remember { mutableStateOf(false) }
    val form = state.form
    val enabled = !state.isLoading && !state.isSaving
    val start = LocalDateTime.parse(form.start)
    val end = LocalDateTime.parse(form.end)
    val formatter = DateTimeFormatter.ofPattern(
        if (form.allDay) "d MMMM yyyy" else "d MMMM yyyy, HH:mm",
        calendarLocale,
    )
    Surface(modifier, color = Color.White, shape = RoundedCornerShape(14.dp)) {
        LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item(key = "title") { Text(stringResource(Res.string.desktop_editor_details), style = MaterialTheme.typography.titleLarge) }
            item(key = "name") {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                    WhoBusyTextField(
                        form.title,
                        { onEvent(EditorCommand.Title(it)) },
                        stringResource(Res.string.event_title),
                        enabled = enabled,
                    )
                }
            }
            item(key = "placement") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        stringResource(Res.string.desktop_editor_placement),
                        color = WhoBusyColors.Primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        WhoBusyChoicePill(
                            stringResource(Res.string.plan_personal),
                            if (form.spaceId == null) WhoBusyColors.Primary else WhoBusyColors.SecondaryText,
                            if (form.spaceId == null) WhoBusyColors.BlueTint else WhoBusyColors.Background,
                            form.spaceId == null,
                            { if (enabled && state.eventId == null) onEvent(EditorCommand.Placement(null)) },
                            Modifier.weight(1f),
                            enabled = enabled && state.eventId == null,
                        )
                        WhoBusyChoicePill(
                            state.spaces.firstOrNull { it.id == form.spaceId }?.name
                                ?: stringResource(Res.string.desktop_editor_in_space),
                            if (form.spaceId != null) WhoBusyColors.Primary else WhoBusyColors.SecondaryText,
                            if (form.spaceId != null) WhoBusyColors.BlueTint else WhoBusyColors.Background,
                            form.spaceId != null,
                            { if (enabled && state.eventId == null && state.spaces.isNotEmpty()) choosingSpace = true },
                            Modifier.weight(1f),
                            enabled = enabled && state.eventId == null && state.spaces.isNotEmpty(),
                        )
                    }
                }
            }
            item(key = "dates") {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        WhoBusyValueButton(
                            stringResource(Res.string.event_start),
                            start.format(formatter),
                            { if (enabled) pickingStart = true },
                            Modifier.weight(1f),
                            enabled = enabled,
                        )
                        WhoBusyValueButton(
                            stringResource(if (form.allDay) Res.string.plan_last_day else Res.string.event_end),
                            (if (form.allDay) end.minusDays(1) else end).format(formatter),
                            { if (enabled) pickingStart = false },
                            Modifier.weight(1f),
                            enabled = enabled,
                        )
                    }
                }
            }
            item(key = "all-day") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.label_all_day))
                        Text(
                            stringResource(Res.string.plan_all_day_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WhoBusyColors.SecondaryText,
                        )
                    }
                    WhoBusyToggle(form.allDay, { if (enabled) onEvent(EditorCommand.AllDay(it)) })
                }
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = WhoBusyColors.Divider)
            }
            item(key = "repeat-status") {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.weight(1f)) {
                            WhoBusyValueButton(
                                stringResource(Res.string.plan_repeat_heading),
                                recurrenceTitle(form.recurrenceRule),
                                { if (enabled) choosingRecurrence = true },
                                enabled = enabled,
                                trailingIcon = UiIcon.ExpandMore,
                            )
                            DropdownMenu(choosingRecurrence, { choosingRecurrence = false }) {
                                recurrenceOptions.forEach { (rule, label) ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(label)) },
                                        onClick = {
                                            onEvent(EditorCommand.Recurrence(rule))
                                            choosingRecurrence = false
                                        },
                                    )
                                }
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            WhoBusyValueButton(
                                stringResource(Res.string.event_status),
                                statusTitle(form.status),
                                { if (enabled) choosingStatus = true },
                                enabled = enabled,
                                trailingIcon = UiIcon.ExpandMore,
                            )
                            DropdownMenu(choosingStatus, { choosingStatus = false }) {
                                EventStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(statusTitle(status)) },
                                        onClick = {
                                            onEvent(EditorCommand.Status(status))
                                            choosingStatus = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item(key = "location") {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                    WhoBusyTextField(
                        form.location,
                        { onEvent(EditorCommand.Location(it)) },
                        stringResource(Res.string.event_location),
                        enabled = enabled,
                    )
                }
            }
            item(key = "description") {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
                    WhoBusyTextField(
                        form.description,
                        { onEvent(EditorCommand.Description(it)) },
                        stringResource(Res.string.event_description),
                        enabled = enabled,
                        singleLine = false,
                    )
                }
            }
            if (form.recurrenceRule != null) {
                item(key = "series") {
                    Text(
                        stringResource(Res.string.desktop_editor_series_notice),
                        color = WhoBusyColors.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
    pickingStart?.let { isStart ->
        val selected = if (isStart) start else if (form.allDay) end.minusDays(1) else end
        EventDateTimePicker(
            selected,
            form.allDay,
            onSelected = { value ->
                val adjusted = if (form.allDay && !isStart) value.toLocalDate().plusDays(1).atStartOfDay() else value
                onEvent(if (isStart) EditorCommand.Start(adjusted) else EditorCommand.End(adjusted))
                pickingStart = null
            },
            onDismiss = { pickingStart = null },
        )
    }
    if (choosingSpace) {
        AlertDialog(
            onDismissRequest = { choosingSpace = false },
            title = { Text(stringResource(Res.string.plan_placement)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(state.spaces, key = { it.id }) { space ->
                        WhoBusyListRow(
                            space.name,
                            onClick = {
                                onEvent(EditorCommand.Placement(space.id))
                                choosingSpace = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                WhoBusyTextButton(stringResource(Res.string.action_cancel), { choosingSpace = false })
            },
        )
    }
}

@Preview(widthDp = 624, heightDp = 832)
@Composable
private fun DesktopEventFieldsPreview() {
    WhoBusyTheme { DesktopEventFields(EventPreviewData.editor, {}) }
}
