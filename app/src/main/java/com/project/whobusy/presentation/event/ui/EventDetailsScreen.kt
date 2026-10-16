package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.model.dayTitle
import com.project.whobusy.presentation.calendar.model.eventTime
import com.project.whobusy.presentation.calendar.model.statusTitle
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData

@Composable
fun EventDetailsScreen(
    event: CalendarEvent,
    canEdit: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    saving: Boolean = false,
    error: String? = null,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(WhoBusyColors.Header).statusBarsPadding()) {
        WhoBusyTopBar("Событие", onBack)
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (!canEdit) {
                Text(
                    "Только просмотр\nМенять событие может его владелец или администратор.",
                    Modifier.fillMaxWidth().background(WhoBusyColors.BlueTint).padding(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                statusTitle(event.status),
                color = calendarColor(event.displayColor),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(event.title, style = MaterialTheme.typography.headlineMedium)
            HorizontalDivider()
            Text(dayTitle(event.start.toLocalDate()), style = MaterialTheme.typography.titleMedium)
            Text(eventTime(event), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (event.location.isNotBlank()) Text(event.location)
            Text(
                if (event.recurrenceRule ==
                    null
                ) {
                    "Не повторяется"
                } else {
                    "Повторяющееся событие · ${event.recurrenceRule}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (event.description.isNotBlank()) {
                Text(
                    "ОПИСАНИЕ",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(event.description)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                WhoBusyAvatar(event.ownerName, calendarColor(event.displayColor))
                Text(event.ownerName, style = MaterialTheme.typography.titleMedium)
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (canEdit) {
                WhoBusyButton("Редактировать событие", onEdit, enabled = !saving)
                WhoBusyButton(
                    if (event.recurrenceRule == null) "Удалить событие" else "Удалить серию",
                    { confirmDelete = true },
                    style = ButtonStyle.Destructive,
                    loading = saving,
                )
            } else {
                WhoBusyButton("Вернуться к календарю", onBack, style = ButtonStyle.Secondary)
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (event.recurrenceRule == null) "Удалить событие?" else "Удалить всю серию?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                WhoBusyTextButton("Удалить", {
                    confirmDelete = false
                    onDelete()
                })
            },
            dismissButton = { WhoBusyTextButton("Отмена", { confirmDelete = false }) },
        )
    }
}

@Preview(name = "Своё событие", widthDp = 390, heightDp = 844)
@Composable
private fun OwnEventPreview() {
    WhoBusyTheme { EventDetailsScreen(CalendarPreviewData.events.first(), true, {}, {}, {}) }
}

@Preview(name = "Только просмотр", widthDp = 390, heightDp = 844)
@Composable
private fun OtherEventPreview() {
    WhoBusyTheme { EventDetailsScreen(CalendarPreviewData.events[1], false, {}, {}, {}) }
}
