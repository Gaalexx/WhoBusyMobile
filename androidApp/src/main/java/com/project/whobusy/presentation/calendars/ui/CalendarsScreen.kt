package com.project.whobusy.presentation.calendars.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyPersonRow
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState

@Composable
fun CalendarsScreen(
    state: CalendarState,
    onEvent: (CalendarCommand) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("calendars-content"),
        contentPadding =
            PaddingValues(
                bottom =
                    90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
    ) {
        item {
            Text(
                stringResource(R.string.calendars_description),
                Modifier
                    .padding(
                        16.dp,
                    ).fillMaxWidth()
                    .background(WhoBusyColors.BlueTint, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.calendars_my_calendar),
                Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        state.user?.let { user ->
            item {
                WhoBusyPersonRow(
                    user.fullName,
                    stringResource(
                        R.string.calendars_self_subtitle,
                    ),
                    calendarColor(user.color),
                    true,
                    {
                    },
                    enabled = false,
                )
            }
        }
        item {
            Text(
                stringResource(R.string.calendars_available),
                Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        items(state.users.filter { it.id in state.accessibleOwnerIds }, key = { it.id }) { user ->
            WhoBusyPersonRow(
                user.fullName,
                if (user.id in
                    state.visibleOwnerIds
                ) {
                    stringResource(R.string.calendars_visible)
                } else {
                    stringResource(R.string.calendars_hidden)
                },
                calendarColor(user.color),
                user.id in state.visibleOwnerIds,
                { onEvent(CalendarCommand.ToggleOwner(user.id)) },
                enabled = !state.isSaving,
            )
        }
        item {
            Text(
                stringResource(R.string.calendars_access_hint),
                Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            state.error?.let {
                Text(
                    stringResource(it),
                    Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 650)
@Composable
private fun CalendarsPreview() {
    WhoBusyTheme { CalendarsScreen(CalendarPreviewData.state, {}) }
}
