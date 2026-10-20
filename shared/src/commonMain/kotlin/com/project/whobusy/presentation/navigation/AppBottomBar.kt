package com.project.whobusy.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiDestination
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyBottomBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.screen_calendar
import com.project.whobusy.resources.screen_calendars
import com.project.whobusy.resources.screen_profile
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

private val destinations = listOf<ScreenTypes>(ScreenTypes.Calendar, ScreenTypes.Calendars, ScreenTypes.Profile)

@Composable
internal fun AppBottomBar(
    currentScreen: ScreenTypes.BottomBarNavigatable,
    onSelect: (ScreenTypes) -> Unit,
) {
    val items =
        listOf(
            UiDestination(stringResource(Res.string.screen_calendar), UiIcon.Calendar),
            UiDestination(stringResource(Res.string.screen_calendars), UiIcon.People),
            UiDestination(stringResource(Res.string.screen_profile), UiIcon.Profile),
        )
    Box(Modifier.fillMaxSize()) {
        WhoBusyBottomBar(
            items = items,
            selected = destinations.indexOfFirst { it == currentScreen },
            onSelect = { onSelect(destinations[it]) },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
                    .fillMaxWidth(0.8f)
                    .testTag("bottom-navigation"),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AppBottomBarPreview() {
    WhoBusyTheme { AppBottomBar(ScreenTypes.Calendars, {}) }
}
