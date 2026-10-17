package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyScreenLayout(
    topBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(WhoBusyColors.Header)) {
        Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
        topBar()
        Box(Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                content()
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ScreenLayoutPreview() {
    WhoBusyTheme { WhoBusyScreenLayout(topBar = { WhoBusyTopBar(stringResource(R.string.ui_calendar)) }) {} }
}
