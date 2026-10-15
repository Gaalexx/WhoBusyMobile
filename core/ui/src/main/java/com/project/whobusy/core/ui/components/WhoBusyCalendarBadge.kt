package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyCalendarBadge() {
    Box(
        Modifier.size(104.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center,
    ) {
        WhoBusyIcon(UiIcon.Calendar, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun CalendarBadgePreview() { WhoBusyTheme { WhoBusyCalendarBadge() } }
