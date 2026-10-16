package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.core.ui.theme.calendarTint
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState

@Composable
fun CalendarLegend(state: CalendarState) {
    val visible = state.users.filter { it.id == state.user?.id || it.id in state.visibleOwnerIds }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        visible.forEach { user ->
            val color = calendarColor(user.color)
            Text(
                if (user.id == state.user?.id) "Вы" else user.fullName.substringBefore(" "),
                Modifier
                    .background(
                        calendarTint(color),
                        RoundedCornerShape(14.dp),
                    ).padding(horizontal = 18.dp, vertical = 6.dp),
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            "Календари: ${visible.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarLegendPreview() {
    WhoBusyTheme { CalendarLegend(CalendarPreviewData.state) }
}
