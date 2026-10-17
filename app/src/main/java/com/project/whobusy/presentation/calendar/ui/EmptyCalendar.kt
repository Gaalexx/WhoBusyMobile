package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyCalendarBadge
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun EmptyCalendar(
    onCreate: () -> Unit,
    onSelectCalendars: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(36.dp))
        WhoBusyCalendarBadge()
        Spacer(Modifier.height(36.dp))
        Text("Пока никаких событий", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "Добавьте свои планы или включите\nдоступные календари коллег.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        WhoBusyButton("Создать событие", onCreate)
        Spacer(Modifier.height(16.dp))
        WhoBusyButton("Выбрать календари", onSelectCalendars, style = ButtonStyle.Secondary)
        Spacer(Modifier.height(90.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Preview(widthDp = 390, heightDp = 510, showBackground = true)
@Composable
private fun EmptyCalendarPreview() {
    WhoBusyTheme { EmptyCalendar({}, {}) }
}
