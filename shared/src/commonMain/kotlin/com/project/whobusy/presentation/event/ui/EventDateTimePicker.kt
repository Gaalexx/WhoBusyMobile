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
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.action_next
import com.project.whobusy.resources.action_select
import com.project.whobusy.resources.label_time
import com.project.whobusy.time.Instant
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.ZoneOffset
import com.project.whobusy.time.atOffset
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.atTime
import com.project.whobusy.time.of
import com.project.whobusy.time.ofEpochMilli
import com.project.whobusy.time.toEpochMilli
import com.project.whobusy.time.toInstant
import com.project.whobusy.time.toLocalDate
import com.project.whobusy.time.value
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

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
                WhoBusyTextButton(
                    stringResource(if (allDay) Res.string.action_select else Res.string.action_next),
                    {
                        datePicker.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                            if (allDay) onSelected(date.atStartOfDay()) else selectedDate = date
                        }
                    }
                )
            },
            dismissButton = { WhoBusyTextButton(stringResource(Res.string.action_cancel), onDismiss) },
        ) { DatePicker(datePicker) }
    } else {
        val time = rememberTimePickerState(value.hour, value.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.label_time)) },
            text = { TimePicker(time) },
            confirmButton = {
                WhoBusyTextButton(
                    stringResource(Res.string.action_select),
                    {
                        onSelected(selectedDate!!.atTime(time.hour, time.minute))
                    }
                )
            },
            dismissButton = { WhoBusyTextButton(stringResource(Res.string.action_cancel), onDismiss) },
        )
    }
}

@Preview
@Composable
private fun DateTimePickerPreview() {
    WhoBusyTheme { EventDateTimePicker(LocalDateTime.of(2026, 10, 15, 14, 0), false, {}, {}) }
}
