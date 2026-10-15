package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyIconButton(
    icon: UiIcon,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    IconButton(onClick, modifier.size(48.dp)) {
        WhoBusyIcon(icon, description, tint = tint)
    }
}

@Composable
fun WhoBusyAddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick, modifier, shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        WhoBusyIcon(UiIcon.Add, "Создать событие", tint = MaterialTheme.colorScheme.onPrimary)
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonsPreview() {
    WhoBusyTheme {
        Row {
            WhoBusyIconButton(UiIcon.Back, "Назад", {})
            WhoBusyAddButton({})
        }
    }
}
