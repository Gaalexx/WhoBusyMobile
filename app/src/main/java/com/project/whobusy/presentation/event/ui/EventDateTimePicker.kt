package com.project.whobusy.presentation.event.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDateTimePicker(
    value: LocalDateTime,
    allDay: Boolean,
    onSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val datePicker =
        rememberDatePickerState(
            initialSelectedDateMillis =
                value
                    .toLocalDate()
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli(),
        )
    if (selectedDate == null) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                WhoBusyTextButton(if (allDay) "Выбрать" else "Далее", {
                    datePicker.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                        if (allDay) onSelected(date.atStartOfDay()) else selectedDate = date
                    }
                })
            },
            dismissButton = { WhoBusyTextButton("Отмена", onDismiss) },
        ) { DatePicker(datePicker) }
    } else {
        val time = rememberTimePickerState(value.hour, value.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Время") },
            text = { TimePicker(time) },
            confirmButton = {
                WhoBusyTextButton("Выбрать", { onSelected(selectedDate!!.atTime(time.hour, time.minute)) })
            },
            dismissButton = { WhoBusyTextButton("Отмена", onDismiss) },
        )
    }
}

@Preview
@Composable
private fun DateTimePickerPreview() {
    WhoBusyTheme { EventDateTimePicker(LocalDateTime.of(2026, 10, 15, 14, 0), false, {}, {}) }
}
