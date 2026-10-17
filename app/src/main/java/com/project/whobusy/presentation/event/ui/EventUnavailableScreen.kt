package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.ui.CalendarLoadingError

@Composable
internal fun EventUnavailableScreen(
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar("Событие", onBack) }) {
        CalendarLoadingError(if (loading) null else error ?: "Событие недоступно", onRetry)
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
