package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_action_back
import com.project.whobusy.resources.ui_action_create_event
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

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
fun WhoBusyAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick,
        modifier,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        WhoBusyIcon(
            UiIcon.Add,
            stringResource(Res.string.ui_action_create_event),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonsPreview() {
    WhoBusyTheme {
        Row {
            WhoBusyIconButton(UiIcon.Back, stringResource(Res.string.ui_action_back), {})
            WhoBusyAddButton({})
        }
    }
}
