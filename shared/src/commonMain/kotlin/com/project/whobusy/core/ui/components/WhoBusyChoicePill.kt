package com.project.whobusy.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_all_day
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyChoicePill(
    text: String,
    color: Color,
    background: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier.then(if (selected) Modifier.border(1.dp, color, shape) else Modifier),
        shape = shape,
        color = background,
    ) {
        Box(
            Modifier
                .heightIn(min = 28.dp)
                .selectable(selected, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, color = color, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChoicePillPreview() {
    WhoBusyTheme {
        WhoBusyChoicePill(
            stringResource(
                Res.string.ui_all_day,
            ),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            true,
            {
            },
        )
    }
}
