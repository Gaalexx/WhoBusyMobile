package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.*
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.model.*
import com.project.whobusy.presentation.event.model.EventFormState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EventEditorFields(state: EventFormState, onChange: (EventFormState) -> Unit) {
    var editingStart by remember { mutableStateOf<Boolean?>(null) }
    var showStatus by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern(if (state.allDay) "d MMMM yyyy" else "d MMMM yyyy, HH:mm", calendarLocale)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WhoBusyTextField(state.title, { onChange(state.copy(title = it, validationError = null)) }, "Название")
        WhoBusySwitchRow("Весь день", state.allDay) { checked ->
            val date = LocalDateTime.parse(state.start).toLocalDate()
            onChange(state.copy(allDay = checked,
                start = (if (checked) date.atStartOfDay() else date.atTime(9, 0)).toString(),
                end = (if (checked) date.plusDays(1).atStartOfDay() else date.atTime(10, 0)).toString()))
        }
        WhoBusyValueButton("Начало", LocalDateTime.parse(state.start).format(formatter), { editingStart = true })
        WhoBusyValueButton(if (state.allDay) "Окончание (не включительно)" else "Окончание",
            LocalDateTime.parse(state.end).format(formatter), { editingStart = false })
        Box {
            WhoBusyValueButton("Статус", statusTitle(state.status), { showStatus = true })
            DropdownMenu(showStatus, { showStatus = false }) {
                EventStatus.entries.forEach { status ->
                    DropdownMenuItem(text = { Text(statusTitle(status)) }, onClick = { showStatus = false; onChange(state.copy(status = status)) })
                }
            }
        }
        WhoBusyTextField(state.location, { onChange(state.copy(location = it)) }, "Место")
        WhoBusyTextField(state.description, { onChange(state.copy(description = it)) }, "Описание", singleLine = false)
    }
    editingStart?.let { isStart ->
        EventDateTimePicker(LocalDateTime.parse(if (isStart) state.start else state.end), state.allDay,
            onSelected = { value ->
                onChange(if (isStart) state.copy(start = value.toString()) else state.copy(end = value.toString()))
                editingStart = null
            }, onDismiss = { editingStart = null })
    }
}

@Preview
@Composable
private fun EditorFieldsPreview() {
    WhoBusyTheme { EventEditorFields(EventFormState.initial(java.time.LocalDate.of(2026, 10, 15), null), {}) }
}
