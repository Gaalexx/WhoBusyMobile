package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyEventChip
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand

@Composable
internal fun DayAllDayEvents(
    events: List<CalendarEvent>,
    onEvent: (CalendarCommand) -> Unit,
) {
    if (events.isEmpty()) return
    LazyColumn(
        Modifier.fillMaxWidth().heightIn(max = 112.dp).testTag("day-all-day-events"),
    ) {
        items(events, key = { "${it.id}:${it.start}" }, contentType = { "all-day-event" }) { event ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.label_all_day),
                    Modifier.width(80.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WhoBusyEventChip(
                    stringResource(R.string.event_owner_title, event.ownerName.substringBefore(" "), event.title),
                    eventColor(event),
                    { onEvent(CalendarCommand.OpenEvent(event)) },
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "Много событий на весь день", widthDp = 390, heightDp = 160, showBackground = true)
@Composable
private fun ManyAllDayEventsPreview() {
    val events = List(100) { index -> CalendarPreviewData.events.last().copy(id = index, title = "План ${index + 1}") }
    WhoBusyTheme { DayAllDayEvents(events, {}) }
}
