package com.project.whobusy.presentation.calendar.ui

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun CalendarLoadingError(
    @StringRes error: Int?,
    onRetry: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        if (error == null) {
            CircularProgressIndicator()
            Text(stringResource(R.string.calendar_loading))
        } else {
            Text(stringResource(error), color = MaterialTheme.colorScheme.error)
            WhoBusyButton(stringResource(R.string.action_retry), onRetry)
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
    WhoBusyTheme { CalendarLoadingError(R.string.error_calendar_load, {}) }
}
