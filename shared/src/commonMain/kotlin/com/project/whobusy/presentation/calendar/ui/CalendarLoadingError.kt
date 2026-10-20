package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_retry
import com.project.whobusy.resources.calendar_loading
import com.project.whobusy.resources.error_calendar_load
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CalendarLoadingError(
    error: StringResource?,
    onRetry: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        if (error == null) {
            CircularProgressIndicator()
            Text(stringResource(Res.string.calendar_loading))
        } else {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error)
            WhoBusyButton(stringResource(Res.string.action_retry), onRetry)
        }
    }
}

@Preview(name = "Загрузка", widthDp = 390, heightDp = 300)
@Composable
private fun LoadingPreview() {
    WhoBusyTheme { CalendarLoadingError(null, {}) }
}

@Preview(name = "Ошибка", widthDp = 390, heightDp = 300)
@Composable
private fun ErrorPreview() {
    WhoBusyTheme { CalendarLoadingError(Res.string.error_calendar_load, {}) }
}
