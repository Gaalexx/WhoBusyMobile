package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyEventChip
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
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
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(events, key = { it.id }) { event ->
            val date = event.start.format(DateTimeFormatter.ofPattern("dd.MM"))
            WhoBusyEventChip(
                "$date · ${event.ownerName.substringBefore(" ")} · ${event.title}",
                calendarColor(event.displayColor),
                { onEvent(CalendarCommand.OpenEvent(event)) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeekAllDayEventsPreview() {
    WhoBusyTheme { WeekAllDayEvents(CalendarPreviewData.state, {}) }
}
