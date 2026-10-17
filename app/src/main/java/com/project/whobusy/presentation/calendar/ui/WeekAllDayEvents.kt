package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
    val events = remember(state.events, start) {
        state.events.filter { it.allDay && it.start < start.plusWeeks(1) && it.end > start }
    }
    val dateFormat = remember { DateTimeFormatter.ofPattern("dd.MM") }
    if (events.isEmpty()) return
    key(start) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 112.dp)
                .testTag("week-all-day-events"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(events, key = { "${it.id}:${it.start}" }, contentType = { "all-day-event" }) { event ->
                val date = event.start.format(dateFormat)
                WhoBusyEventChip(
                    stringResource(R.string.event_summary, date, event.ownerName.substringBefore(" "), event.title),
                    eventColor(event),
                    { onEvent(CalendarCommand.OpenEvent(event)) },
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeekAllDayEventsPreview() {
    WhoBusyTheme { WeekAllDayEvents(CalendarPreviewData.state, {}) }
}
