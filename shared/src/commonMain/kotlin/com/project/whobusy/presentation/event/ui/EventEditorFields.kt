package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusySwitchRow
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyValueButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.calendar.ui.statusTitle
import com.project.whobusy.presentation.event.model.EventFormState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.event_date_pattern
import com.project.whobusy.resources.event_date_time_pattern
import com.project.whobusy.resources.event_description
import com.project.whobusy.resources.event_end
import com.project.whobusy.resources.event_end_exclusive
import com.project.whobusy.resources.event_location
import com.project.whobusy.resources.event_start
import com.project.whobusy.resources.event_status
import com.project.whobusy.resources.event_title
import com.project.whobusy.resources.label_all_day
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.atTime
import com.project.whobusy.time.format
import com.project.whobusy.time.of
import com.project.whobusy.time.parse
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.toLocalDate
import com.project.whobusy.time.value
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EventEditorFields(
    state: EventFormState,
    onChange: (EventFormState) -> Unit,
) {
    var editingStart by remember { mutableStateOf<Boolean?>(null) }
    var showStatus by remember { mutableStateOf(false) }
    val formatter =
        DateTimeFormatter.ofPattern(
            stringResource(if (state.allDay) Res.string.event_date_pattern else Res.string.event_date_time_pattern),
            calendarLocale,
        )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhoBusyTextField(
            state.title,
            { onChange(state.copy(title = it, validationError = null)) },
            stringResource(Res.string.event_title),
        )
        WhoBusySwitchRow(stringResource(Res.string.label_all_day), state.allDay) { checked ->
            val date = LocalDateTime.parse(state.start).toLocalDate()
            onChange(
                state.copy(
                    allDay = checked,
                    start = (if (checked) date.atStartOfDay() else date.atTime(9, 0)).toString(),
                    end = (if (checked) date.plusDays(1).atStartOfDay() else date.atTime(10, 0)).toString(),
                ),
            )
        }
        WhoBusyValueButton(
            stringResource(Res.string.event_start),
            LocalDateTime.parse(state.start).format(formatter),
            { editingStart = true },
        )
        WhoBusyValueButton(
            stringResource(if (state.allDay) Res.string.event_end_exclusive else Res.string.event_end),
            LocalDateTime.parse(state.end).format(formatter),
            { editingStart = false },
        )
        Box {
            WhoBusyValueButton(stringResource(Res.string.event_status), statusTitle(state.status), { showStatus = true })
            DropdownMenu(showStatus, { showStatus = false }) {
                EventStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(statusTitle(status)) },
                        onClick = {
                            showStatus = false
                            onChange(state.copy(status = status))
                        }
                    )
                }
            }
        }
        WhoBusyTextField(
            state.location,
            { onChange(state.copy(location = it)) },
            stringResource(Res.string.event_location),
        )
        WhoBusyTextField(
            state.description,
            { onChange(state.copy(description = it)) },
            stringResource(Res.string.event_description),
            singleLine = false,
        )
    }
    editingStart?.let { isStart ->
        EventDateTimePicker(
            LocalDateTime.parse(if (isStart) state.start else state.end),
            state.allDay,
            onSelected = { value ->
                onChange(if (isStart) state.copy(start = value.toString()) else state.copy(end = value.toString()))
                editingStart = null
            },
            onDismiss = { editingStart = null },
        )
    }
}

@Preview
@Composable
private fun EditorFieldsPreview() {
    WhoBusyTheme { EventEditorFields(EventFormState.initial(LocalDate.of(2026, 10, 15), null), {}) }
}
