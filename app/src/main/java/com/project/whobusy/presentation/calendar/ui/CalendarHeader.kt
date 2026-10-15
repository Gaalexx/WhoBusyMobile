package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.*
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.*
import com.project.whobusy.presentation.calendar.viewmodel.*
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHeader(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    WhoBusyTabs(CalendarMode.entries.map { it.title }, state.mode.ordinal) {
        onEvent(CalendarCommand.SelectMode(CalendarMode.entries[it]))
    }
    Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        WhoBusyTextButton(monthTitle(state.selectedDate), { showPicker = true },
            color = MaterialTheme.colorScheme.onSurface, textStyle = MaterialTheme.typography.titleMedium)
        WhoBusyTextButton("Сегодня", { onEvent(CalendarCommand.Today) })
    }
    if (state.mode != CalendarMode.Month) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 4.dp)) {
            val start = weekStart(state.selectedDate)
            weekLabels.forEachIndexed { index, label ->
                val date = start.plusDays(index.toLong())
                WhoBusyDateCell(date.dayOfMonth.toString(), dayTitle(date), date == state.selectedDate,
                    { onEvent(CalendarCommand.SelectDate(date)) }, Modifier.weight(1f), weekday = label)
            }
        }
    }
    if (showPicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = state.selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = { WhoBusyTextButton("Выбрать", {
                picker.selectedDateMillis?.let { onEvent(CalendarCommand.SelectDate(Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate())) }
                showPicker = false
            }) },
            dismissButton = { WhoBusyTextButton("Отмена", { showPicker = false }) },
        ) { DatePicker(picker) }
    }
}

@Preview
@Composable
private fun CalendarHeaderPreview() {
    WhoBusyTheme { Column { CalendarHeader(com.project.whobusy.presentation.calendar.preview.CalendarPreviewData.state, {}) } }
}
