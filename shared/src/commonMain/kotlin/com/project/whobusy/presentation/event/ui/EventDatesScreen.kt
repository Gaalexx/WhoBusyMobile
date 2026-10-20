package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyChoicePill
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyRadioRow
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyToggle
import com.project.whobusy.core.ui.components.WhoBusyValueButton
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.calendar.ui.statusTitle
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorState
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.error_event_end_after_start
import com.project.whobusy.resources.event_end
import com.project.whobusy.resources.event_start
import com.project.whobusy.resources.label_all_day
import com.project.whobusy.resources.plan_dates_all_day_hint
import com.project.whobusy.resources.plan_dates_repeat
import com.project.whobusy.resources.plan_done
import com.project.whobusy.resources.plan_first_day
import com.project.whobusy.resources.plan_last_day
import com.project.whobusy.resources.plan_repeat_heading
import com.project.whobusy.resources.plan_status_heading
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.format
import com.project.whobusy.time.minusDays
import com.project.whobusy.time.parse
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.toLocalDate
import com.project.whobusy.time.value
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EventDatesRoot(
    viewModel: EventEditorViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventDatesScreen(state, viewModel::onEvent, onBack)
}

@Composable
fun EventDatesScreen(
    state: EditorState,
    onEvent: (EditorCommand) -> Unit,
    onBack: () -> Unit,
) {
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
    val form = state.form
    val start = LocalDateTime.parse(form.start)
    val end = LocalDateTime.parse(form.end)
    val validDates = end > start
    val formatter =
        DateTimeFormatter.ofPattern(
            if (form.allDay) "d MMMM yyyy" else "d MMMM yyyy, HH:mm",
            calendarLocale,
        )
    EventPage(
        stringResource(Res.string.plan_dates_repeat),
        onBack,
        footer = {
            WhoBusyButton(
                stringResource(Res.string.plan_done),
                onBack,
                Modifier.padding(16.dp),
                enabled = validDates,
            )
        },
    ) {
        item {
            Column {
                Spacer(Modifier.height(16.dp))
                WhoBusyListRow(
                    stringResource(Res.string.label_all_day),
                    stringResource(Res.string.plan_dates_all_day_hint),
                    trailing = { WhoBusyToggle(form.allDay, { onEvent(EditorCommand.AllDay(it)) }) },
                )
                Spacer(Modifier.height(16.dp))
                WhoBusyValueButton(
                    stringResource(if (form.allDay) Res.string.plan_first_day else Res.string.event_start),
                    start.format(formatter),
                    { pickingStart = true },
                    Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(10.dp))
                WhoBusyValueButton(
                    stringResource(if (form.allDay) Res.string.plan_last_day else Res.string.event_end),
                    (if (form.allDay) end.minusDays(1) else end).format(formatter),
                    { pickingStart = false },
                    Modifier.padding(horizontal = 16.dp),
                )
                if (!validDates) {
                    Text(
                        stringResource(Res.string.error_event_end_after_start),
                        Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(20.dp))
                WhoBusySectionTitle(stringResource(Res.string.plan_status_heading), Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EventStatus.entries.forEach { status ->
                        val color =
                            when (status) {
                                EventStatus.BUSY -> WhoBusyColors.Primary
                                EventStatus.FREE -> WhoBusyColors.Green
                                EventStatus.TENTATIVE -> Color(0xFF94691C)
                            }
                        val tint =
                            when (status) {
                                EventStatus.BUSY -> WhoBusyColors.BlueTint
                                EventStatus.FREE -> WhoBusyColors.GreenTint
                                EventStatus.TENTATIVE -> Color(0xFFFFF5E1)
                            }
                        WhoBusyChoicePill(
                            statusTitle(status), color, tint, form.status == status,
                            {
                                onEvent(EditorCommand.Status(status))
                            },
                            Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(26.dp))
                WhoBusySectionTitle(stringResource(Res.string.plan_repeat_heading), Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
                androidx.compose.foundation.layout.Column(Modifier.selectableGroup()) {
                    recurrenceOptions.forEach { (rule, label) ->
                        WhoBusyRadioRow(
                            stringResource(label),
                            form.recurrenceRule == rule,
                            { onEvent(EditorCommand.Recurrence(rule)) },
                        )
                    }
                }
            }
        }
    }
    pickingStart?.let { isStart ->
        val selected =
            if (isStart) {
                start
            } else if (form.allDay) {
                end.minusDays(1)
            } else {
                end
            }
        EventDateTimePicker(
            selected,
            form.allDay,
            onSelected = { value ->
                val adjusted = if (form.allDay && !isStart) value.toLocalDate().plusDays(1).atStartOfDay() else value
                onEvent(if (isStart) EditorCommand.Start(adjusted) else EditorCommand.End(adjusted))
                pickingStart = null
            },
            onDismiss = { pickingStart = null },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DatesPreview() {
    WhoBusyTheme {
        EventDatesScreen(
            EventPreviewData.editor.copy(
                form =
                    EventPreviewData.editor.form.copy(
                        allDay = true,
                        start = "2026-10-16T00:00",
                        end = "2026-10-19T00:00",
                        recurrenceRule = null,
                    ),
            ),
            {},
            {},
        )
    }
}
