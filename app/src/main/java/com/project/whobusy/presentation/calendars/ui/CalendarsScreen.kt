package com.project.whobusy.presentation.calendars.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyPersonRow
import com.project.whobusy.core.ui.theme.*
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.*

@Composable
fun CalendarsScreen(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            Text("Ваша команда — в одном календаре\nВыберите, чьи события показывать рядом с вашими.",
                Modifier.padding(16.dp).fillMaxWidth().background(WhoBusyColors.BlueTint, RoundedCornerShape(10.dp)).padding(12.dp),
                color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            Text("МОЙ КАЛЕНДАРЬ", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        }
        state.user?.let { user -> item {
            WhoBusyPersonRow(user.fullName, "Вы · Всегда отображается", calendarColor(user.color), true, {}, enabled = false)
        } }
        item { Text("ДОСТУПНЫ МНЕ", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium) }
        items(state.users.filter { it.id in state.accessibleOwnerIds }, key = { it.id }) { user ->
            WhoBusyPersonRow(user.fullName, if (user.id in state.visibleOwnerIds) "Календарь доступен" else "Не отображается",
                calendarColor(user.color), user.id in state.visibleOwnerIds,
                { onEvent(CalendarCommand.ToggleOwner(user.id)) }, enabled = !state.isSaving)
        }
        item {
            Text("Здесь только календари, к которым вам открыли доступ. Свой всегда включён.",
                Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            state.error?.let { Text(it, Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Preview(widthDp = 390, heightDp = 650)
@Composable
private fun CalendarsPreview() { WhoBusyTheme { CalendarsScreen(CalendarPreviewData.state, {}) } }
