package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.core.ui.theme.calendarTint
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState

@Composable
fun CalendarLegend(state: CalendarState) {
    val visible = remember(state.users, state.user?.id, state.visibleOwnerIds) {
        state.users.filter { it.id == state.user?.id || it.id in state.visibleOwnerIds }
    }
    LazyRow(
        Modifier.fillMaxWidth().testTag("calendar-legend"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(visible, key = { it.id }, contentType = { "calendar-owner" }) { user ->
            val color = calendarColor(user.color)
            Text(
                if (user.id ==
                    state.user?.id
                ) {
                    stringResource(R.string.label_you)
                } else {
                    user.fullName.substringBefore(" ")
                },
                Modifier.testTag("calendar-owner-${user.id}")
                    .background(
                        calendarTint(color),
                        RoundedCornerShape(14.dp),
                    ).padding(horizontal = 18.dp, vertical = 6.dp),
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarLegendPreview() {
    WhoBusyTheme { CalendarLegend(CalendarPreviewData.state) }
}
