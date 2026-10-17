package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyEventChip
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.model.weekStart
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import java.time.format.DateTimeFormatter

@Composable
fun WeekAllDayEvents(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    val start = weekStart(state.selectedDate).atStartOfDay()
    val events = state.events.filter { it.allDay && it.start < start.plusWeeks(1) && it.end > start }
    if (events.isEmpty()) return
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 112.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        events.forEach { event ->
            val date = event.start.format(DateTimeFormatter.ofPattern("dd.MM"))
            WhoBusyEventChip(
                stringResource(R.string.event_summary, date, event.ownerName.substringBefore(" "), event.title),
                eventColor(event),
                { onEvent(CalendarCommand.OpenEvent(event)) },
                Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeekAllDayEventsPreview() {
    WhoBusyTheme { WeekAllDayEvents(CalendarPreviewData.state, {}) }
}
