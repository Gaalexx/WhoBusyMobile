package com.project.whobusy.presentation.event.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import java.time.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDateTimePicker(value: LocalDateTime, allDay: Boolean, onSelected: (LocalDateTime) -> Unit, onDismiss: () -> Unit) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val datePicker = rememberDatePickerState(initialSelectedDateMillis = value.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
    if (selectedDate == null) {
        DatePickerDialog(onDismissRequest = onDismiss,
            confirmButton = { WhoBusyTextButton(if (allDay) "Выбрать" else "Далее", {
                datePicker.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                    if (allDay) onSelected(date.atStartOfDay()) else selectedDate = date
                }
            }) },
            dismissButton = { WhoBusyTextButton("Отмена", onDismiss) },
        ) { DatePicker(datePicker) }
    } else {
        val time = rememberTimePickerState(value.hour, value.minute, is24Hour = true)
        AlertDialog(onDismissRequest = onDismiss, title = { Text("Время") }, text = { TimePicker(time) },
            confirmButton = { WhoBusyTextButton("Выбрать", { onSelected(selectedDate!!.atTime(time.hour, time.minute)) }) },
            dismissButton = { WhoBusyTextButton("Отмена", onDismiss) })
    }
}

@Preview
@Composable
private fun DateTimePickerPreview() {
    WhoBusyTheme { EventDateTimePicker(LocalDateTime.of(2026, 10, 15, 14, 0), false, {}, {}) }
}
