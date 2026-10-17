package com.project.whobusy.presentation.event.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.ui.CalendarLoadingError

@Composable
internal fun EventUnavailableScreen(
    loading: Boolean,
    @StringRes error: Int?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.screen_event), onBack) }) {
        CalendarLoadingError(if (loading) null else error ?: R.string.event_unavailable, onRetry)
    }
}

@Preview(name = "Недоступно", widthDp = 390, heightDp = 844)
@Composable
private fun EventUnavailablePreview() {
    WhoBusyTheme { EventUnavailableScreen(false, null, {}, {}) }
}

@Preview(name = "Загрузка", widthDp = 390, heightDp = 844)
@Composable
private fun EventLoadingPreview() {
    WhoBusyTheme { EventUnavailableScreen(true, null, {}, {}) }
}
