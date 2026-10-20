package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.ui.eventTime
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.ui.recurrenceTitle
import com.project.whobusy.presentation.event.ui.visibilityTitle
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.action_delete
import com.project.whobusy.resources.desktop_close_details
import com.project.whobusy.resources.desktop_plan
import com.project.whobusy.resources.event_delete_confirmation
import com.project.whobusy.resources.event_delete_series_confirmation
import com.project.whobusy.resources.event_delete_warning
import com.project.whobusy.resources.plan_busy
import com.project.whobusy.resources.plan_busy_notice_body
import com.project.whobusy.resources.plan_delete_action
import com.project.whobusy.resources.plan_delete_series
import com.project.whobusy.resources.plan_edit_action
import com.project.whobusy.resources.plan_no_description
import com.project.whobusy.resources.plan_no_location
import com.project.whobusy.resources.plan_personal
import com.project.whobusy.resources.plan_series_hint
import com.project.whobusy.resources.plan_shared_item
import com.project.whobusy.resources.plan_visibility_heading
import com.project.whobusy.resources.plan_visibility_public
import com.project.whobusy.time.toLocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopPlanDetailsScreen(
    event: CalendarEvent,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    saving: Boolean,
    error: StringResource?,
    spaces: List<Space>,
) {
    var confirmDelete by remember(event.id) { mutableStateOf(false) }
    val editable = event.canEdit && !event.busyOnly
    val space = spaces.firstOrNull { it.id == event.spaceId }
    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth().height(72.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.desktop_plan), Modifier.weight(1f), fontSize = 16.sp)
            IconButton(onBack, enabled = !saving) {
                WhoBusyIcon(UiIcon.Close, stringResource(Res.string.desktop_close_details))
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Text(
                    space?.name ?: stringResource(if (event.busyOnly) Res.string.plan_busy else Res.string.plan_personal),
                    Modifier.background(DesktopPalette.Selection, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    color = DesktopPalette.Accent, fontSize = 12.sp,
                )
                Spacer(Modifier.height(24.dp))
                Text(event.title, color = DesktopPalette.Text, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(event.ownerName, color = DesktopPalette.Muted, fontSize = 14.sp)
                HorizontalDivider(Modifier.padding(top = 28.dp), color = DesktopPalette.Border)
            }
            item {
                DesktopPlanInfo(UiIcon.Calendar, dayTitle(event.start.toLocalDate()), eventTime(event))
            }
            if (event.busyOnly) {
                item {
                    Text(
                        stringResource(Res.string.plan_busy_notice_body), color = DesktopPalette.Muted,
                        fontSize = 14.sp, lineHeight = 22.sp,
                    )
                }
            } else {
                item {
                    DesktopPlanInfo(
                        UiIcon.Profile,
                        event.location.ifBlank { stringResource(Res.string.plan_no_location) },
                        event.description.ifBlank { stringResource(Res.string.plan_no_description) },
                    )
                }
                item {
                    DesktopPlanInfo(
                        UiIcon.Calendar,
                        recurrenceTitle(event.recurrenceRule),
                        if (event.recurrenceRule != null) stringResource(Res.string.plan_series_hint) else null,
                    )
                }
                item {
                    Text(stringResource(Res.string.plan_visibility_heading), color = DesktopPalette.Accent, fontSize = 12.sp)
                }
                if (space != null) {
                    item { Text("${space.name} · ${stringResource(Res.string.plan_visibility_public)}", fontSize = 14.sp) }
                } else if (event.canEdit) {
                    items(spaces, key = { it.id }) { sharedSpace ->
                        Text(
                            stringResource(
                                Res.string.plan_shared_item, sharedSpace.name,
                                visibilityTitle(event.shares.firstOrNull { it.spaceId == sharedSpace.id }?.visibility),
                            ), fontSize = 14.sp, color = DesktopPalette.Text,
                        )
                    }
                }
            }
        }
        error?.let {
            Text(stringResource(it), Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        if (editable) {
            Column(Modifier.padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                WhoBusyButton(stringResource(Res.string.plan_edit_action), onEdit, enabled = !saving)
                WhoBusyButton(
                    stringResource(if (event.recurrenceRule == null) Res.string.plan_delete_action else Res.string.plan_delete_series),
                    { confirmDelete = true }, style = ButtonStyle.Destructive, loading = saving,
                )
            }
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = {
                Text(
                    stringResource(
                        if (event.recurrenceRule == null) Res.string.event_delete_confirmation else Res.string.event_delete_series_confirmation,
                    ),
                )
            },
            text = { Text(stringResource(Res.string.event_delete_warning)) },
            confirmButton = {
                WhoBusyTextButton(stringResource(Res.string.action_delete), {
                    confirmDelete = false
                    onDelete()
                })
            },
            dismissButton = { WhoBusyTextButton(stringResource(Res.string.action_cancel), { confirmDelete = false }) },
        )
    }
}

@Composable
private fun DesktopPlanInfo(icon: UiIcon, title: String, subtitle: String?) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        WhoBusyIcon(icon, tint = DesktopPalette.Accent)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontSize = 14.sp, color = DesktopPalette.Text)
            subtitle?.let { Text(it, fontSize = 14.sp, lineHeight = 22.sp, color = DesktopPalette.Muted) }
        }
    }
}

@Preview(widthDp = 288, heightDp = 808)
@Composable
private fun DesktopPlanDetailsPreview() {
    WhoBusyTheme { DesktopPlanDetailsScreen(EventPreviewData.event, {}, {}, {}, false, null, EventPreviewData.spaces) }
}

@Preview(name = "Только занятость", widthDp = 288, heightDp = 808)
@Composable
private fun DesktopBusyPlanDetailsPreview() {
    WhoBusyTheme {
        DesktopPlanDetailsScreen(
            EventPreviewData.event.copy(title = "Занято", busyOnly = true, canEdit = false), {}, {}, {}, false, null, emptyList(),
        )
    }
}
