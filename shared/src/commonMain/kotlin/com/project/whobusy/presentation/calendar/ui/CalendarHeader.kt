package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyTabs
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.monthTitle
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.action_select
import com.project.whobusy.resources.action_today
import com.project.whobusy.time.Instant
import com.project.whobusy.time.ZoneOffset
import com.project.whobusy.time.atOffset
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.ofEpochMilli
import com.project.whobusy.time.toEpochMilli
import com.project.whobusy.time.toInstant
import com.project.whobusy.time.toLocalDate
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHeader(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    WhoBusyTabs(CalendarMode.entries.map { stringResource(it.titleRes) }, state.mode.ordinal) {
        onEvent(CalendarCommand.SelectMode(CalendarMode.entries[it]))
    }
    Row(
        Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        WhoBusyTextButton(
            monthTitle(state.selectedDate),
            { showPicker = true },
            color = MaterialTheme.colorScheme.onSurface,
            textStyle = MaterialTheme.typography.titleMedium,
        )
        WhoBusyTextButton(stringResource(Res.string.action_today), { onEvent(CalendarCommand.Today) })
    }
    if (showPicker) {
        val picker =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    state.selectedDate
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                        .toEpochMilli(),
            )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                WhoBusyTextButton(
                    stringResource(Res.string.action_select),
                    {
                        picker.selectedDateMillis?.let {
                            onEvent(
                                CalendarCommand.SelectDate(
                                    Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
                                ),
                            )
                        }
                        showPicker = false
                    }
                )
            },
            dismissButton = { WhoBusyTextButton(stringResource(Res.string.action_cancel), { showPicker = false }) },
        ) { DatePicker(picker) }
    }
}

@Preview
@Composable
private fun CalendarHeaderPreview() {
    WhoBusyTheme {
        Column { CalendarHeader(com.project.whobusy.presentation.calendar.preview.CalendarPreviewData.state, {}) }
    }
}
