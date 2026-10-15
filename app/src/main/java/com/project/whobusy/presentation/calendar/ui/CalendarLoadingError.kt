package com.project.whobusy.presentation.calendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun CalendarLoadingError(error: String?, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
        if (error == null) {
            CircularProgressIndicator()
            Text("Загружаем календарь…")
        } else {
            Text(error, color = MaterialTheme.colorScheme.error)
            WhoBusyButton("Повторить", onRetry)
        }
    }
}

@Preview(name = "Загрузка", widthDp = 390, heightDp = 300)
@Composable
private fun LoadingPreview() { WhoBusyTheme { CalendarLoadingError(null, {}) } }

@Preview(name = "Ошибка", widthDp = 390, heightDp = 300)
@Composable
private fun ErrorPreview() { WhoBusyTheme { CalendarLoadingError("Не удалось загрузить календарь", {}) } }
