package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarTint
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyEventChip(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick, modifier, shape = RoundedCornerShape(14.dp), color = calendarTint(color)) {
        Text(
            text,
            Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun EventChipPreview() {
    WhoBusyTheme { WhoBusyEventChip("Анна · Отпуск", WhoBusyColors.Purple, {}) }
}
