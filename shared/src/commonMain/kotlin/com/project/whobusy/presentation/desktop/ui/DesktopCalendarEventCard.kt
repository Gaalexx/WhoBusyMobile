package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarTint
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.ui.eventColor
import com.project.whobusy.presentation.calendar.ui.eventTime

@Composable
internal fun DesktopCalendarEventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTime: Boolean = true,
    showText: Boolean = true,
    subtitle: String? = null,
) {
    val color = eventColor(event)
    val description = "${event.title}, ${eventTime(event)}"
    Column(
        modifier.clip(RoundedCornerShape(8.dp)).background(calendarTint(color)).clickable(onClick = onClick)
            .semantics { if (!showText) contentDescription = description }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (showText) {
            Text(event.title, color = color, fontSize = 14.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (showTime) {
                Text(subtitle ?: eventTime(event), color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(widthDp = 240, heightDp = 100)
@Composable
private fun DesktopCalendarEventPreview() {
    WhoBusyTheme { DesktopCalendarEventCard(CalendarPreviewData.events.first(), {}, Modifier.fillMaxSize()) }
}

@Preview(name = "Только занятость", widthDp = 240, heightDp = 100)
@Composable
private fun DesktopBusyEventPreview() {
    WhoBusyTheme {
        DesktopCalendarEventCard(CalendarPreviewData.events.first().copy(title = "Занято", busyOnly = true), {}, Modifier.fillMaxSize())
    }
}
