package com.project.whobusy.presentation.event.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.calendar.ui.eventTime
import com.project.whobusy.presentation.calendar.ui.statusTitle
import com.project.whobusy.presentation.event.preview.EventPreviewData
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailsScreen(
    event: CalendarEvent,
    canEdit: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    saving: Boolean = false,
    @StringRes error: Int? = null,
    spaces: List<Space> = emptyList(),
) {
    var confirmDelete by remember(event.id) { mutableStateOf(false) }
    val editable = canEdit && !event.busyOnly
    val space = spaces.firstOrNull { it.id == event.spaceId }
    EventPage(
        stringResource(if (event.busyOnly) R.string.plan_busy else R.string.plan_details),
        { if (!saving) onBack() },
        action = {
            if (editable) {
                WhoBusyIconButton(
                    UiIcon.Edit,
                    stringResource(R.string.plan_edit_action),
                    onEdit,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        footer = {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                if (editable) {
                    WhoBusyButton(stringResource(R.string.plan_edit_action), onEdit, enabled = !saving)
                    Spacer(Modifier.height(16.dp))
                    WhoBusyButton(
                        stringResource(
                            if (event.recurrenceRule ==
                                null
                            ) {
                                R.string.plan_delete_action
                            } else {
                                R.string.plan_delete_series
                            },
                        ),
                        { confirmDelete = true },
                        style = ButtonStyle.Destructive,
                        loading = saving,
                    )
                } else {
                    WhoBusyButton(stringResource(R.string.plan_back_board), onBack, style = ButtonStyle.Secondary)
                }
            }
        },
    ) {
        item {
            Column {
                if (event.busyOnly) {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 72.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WhoBusyIcon(
                            UiIcon.Lock,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(60.dp))
                        Text(stringResource(R.string.plan_busy), style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            event.ownerName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(48.dp))
                    }
                    PlanDateRow(event, busyOnly = true)
                    Spacer(Modifier.height(48.dp))
                    WhoBusyNotice(
                        stringResource(R.string.plan_busy_notice),
                        stringResource(R.string.plan_busy_notice_body),
                        Modifier.padding(horizontal = 16.dp),
                    )
                } else {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
                        Text(
                            space?.name ?: stringResource(R.string.plan_personal),
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(14.dp),
                                ).padding(horizontal = 24.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(event.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(14.dp))
                    }
                    PlanDateRow(event)
                    Spacer(Modifier.height(2.dp))
                    WhoBusyListRow(
                        event.location.ifBlank { stringResource(R.string.plan_no_location) },
                        event.description.ifBlank { stringResource(R.string.plan_no_description) },
                    )
                    Spacer(Modifier.height(2.dp))
                    WhoBusyListRow(
                        recurrenceTitle(event.recurrenceRule),
                        if (event.recurrenceRule == null) null else stringResource(R.string.plan_series_hint),
                    )
                    WhoBusySectionTitle(
                        stringResource(R.string.plan_visibility_heading),
                        Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    )
                    when {
                        space != null -> WhoBusyListRow(space.name, stringResource(R.string.plan_visibility_public))
                        event.spaceId == null && event.canEdit && spaces.isNotEmpty() -> {
                            val items =
                                spaces.map { item ->
                                    stringResource(
                                        R.string.plan_shared_item,
                                        item.name,
                                        visibilityTitle(event.shares.firstOrNull { it.spaceId == item.id }?.visibility),
                                    )
                                }
                            WhoBusyListRow(items.first(), items.drop(1).joinToString("; ").ifBlank { null })
                        }

                        else -> WhoBusyListRow(stringResource(R.string.plan_author, event.ownerName))
                    }
                }
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = {
                Text(
                    stringResource(
                        if (event.recurrenceRule ==
                            null
                        ) {
                            R.string.event_delete_confirmation
                        } else {
                            R.string.event_delete_series_confirmation
                        },
                    ),
                )
            },
            text = { Text(stringResource(R.string.event_delete_warning)) },
            confirmButton = {
                WhoBusyTextButton(
                    stringResource(R.string.action_delete),
                    {
                        confirmDelete = false
                        onDelete()
                    }
                )
            },
            dismissButton = { WhoBusyTextButton(stringResource(R.string.action_cancel), { confirmDelete = false }) },
        )
    }
}

@Composable
private fun PlanDateRow(
    event: CalendarEvent,
    busyOnly: Boolean = false,
) {
    val date =
        event.start.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", calendarLocale)).replaceFirstChar {
            it.titlecase(calendarLocale)
        }
    WhoBusyListRow(
        date,
        if (busyOnly) {
            eventTime(
                event,
            )
        } else {
            stringResource(R.string.plan_repeat_status_value, eventTime(event), statusTitle(event.status))
        },
    )
}

@Preview(name = "Свой план", widthDp = 390, heightDp = 844)
@Composable
private fun OwnPlanPreview() {
    WhoBusyTheme { EventDetailsScreen(EventPreviewData.event, true, {}, {}, {}, spaces = EventPreviewData.spaces) }
}

@Preview(name = "Занятость", widthDp = 390, heightDp = 844)
@Composable
private fun BusyPlanPreview() {
    WhoBusyTheme {
        EventDetailsScreen(
            EventPreviewData.event.copy(
                title = "Занято",
                description = "",
                location = "",
                busyOnly = true,
                canEdit = false,
                shares = emptyList(),
            ),
            false,
            {
            },
            {},
            {},
        )
    }
}

@Preview(name = "Чужой план", widthDp = 390, heightDp = 844)
@Composable
private fun SharedPlanPreview() {
    WhoBusyTheme {
        EventDetailsScreen(EventPreviewData.event.copy(canEdit = false, shares = emptyList()), false, {}, {}, {})
    }
}
