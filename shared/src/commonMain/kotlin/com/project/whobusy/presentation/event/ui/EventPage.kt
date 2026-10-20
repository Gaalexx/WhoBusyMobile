package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar

@Composable
internal fun EventPage(
    title: String,
    onBack: () -> Unit,
    action: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    WhoBusyScreenLayout(topBar = { WhoBusyTopBar(title, onBack, action = { action() }) }) {
        Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), content = content)
            footer()
        }
    }
}
