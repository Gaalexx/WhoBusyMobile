package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.whobusy.core.ui.theme.*
import com.project.whobusy.presentation.calendar.model.*
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.*
import java.time.format.DateTimeFormatter

@Composable
fun WeekCalendar(state: CalendarState, onEvent: (CalendarCommand) -> Unit) {
    val start = weekStart(state.selectedDate)
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    LaunchedEffect(start) { scroll.scrollTo(with(density) { (56.dp * 9).roundToPx() }) }
    Column(Modifier.fillMaxSize()) {
        WeekAllDayEvents(state, onEvent)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll)) {
            val dayWidth = (maxWidth - 34.dp) / 7
            Box(Modifier.fillMaxWidth().height(56.dp * 24)) {
                repeat(24) { hour ->
                    Row(Modifier.offset(y = 56.dp * hour)) {
                        Text("%02d".format(hour), Modifier.width(34.dp).padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                repeat(7) { index ->
                    val date = start.plusDays(index.toLong())
                    Box(Modifier.offset(x = 34.dp + dayWidth * index).width(dayWidth).fillMaxHeight()
                        .clickable {
                            onEvent(CalendarCommand.SelectDate(date))
                            onEvent(CalendarCommand.SelectMode(CalendarMode.Day))
                        }) {
                        Box(Modifier.fillMaxHeight().width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        placeEvents(state.events, date).forEach { placement ->
                            val event = placement.event
                            val width = dayWidth / placement.laneCount
                            Column(Modifier.offset(x = width * placement.lane + 2.dp, y = 56.dp * (placement.startMinute / 60f))
                                .width(width - 4.dp).height((56.dp * ((placement.endMinute - placement.startMinute) / 60f)).coerceAtLeast(36.dp) - 2.dp)
                                .clip(RoundedCornerShape(5.dp)).background(calendarColor(event.displayColor)).padding(3.dp)) {
                                Text(event.start.format(DateTimeFormatter.ofPattern("HH:mm")), color = Color.White, fontSize = 10.sp, maxLines = 1)
                                Text(event.title, color = Color.White, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
        Text("${start.dayOfMonth}–${start.plusDays(6).dayOfMonth} · Нажмите на день",
            Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(widthDp = 390, heightDp = 450, showBackground = true)
@Composable
private fun WeekCalendarPreview() { WhoBusyTheme { WeekCalendar(CalendarPreviewData.state, {}) } }
