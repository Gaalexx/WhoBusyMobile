package com.project.whobusy.presentation.event.ui

import androidx.compose.runtime.Composable
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.calendar.ui.CalendarLoadingError
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.event_unavailable
import com.project.whobusy.resources.screen_event
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun EventUnavailableScreen(
    loading: Boolean,
    error: StringResource?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(Res.string.screen_event), onBack) }) {
        CalendarLoadingError(if (loading) null else error ?: Res.string.event_unavailable, onRetry)
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
