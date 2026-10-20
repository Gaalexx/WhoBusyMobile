package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_start
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyValueButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: UiIcon? = null,
) {
    Surface(
        onClick,
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        enabled = enabled,
    ) {
        Row(
            Modifier.heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
            trailingIcon?.let { WhoBusyIcon(it) }
        }
    }
}

@Preview
@Composable
private fun ValueButtonPreview() {
    WhoBusyTheme { WhoBusyValueButton(stringResource(Res.string.ui_start), "15 октября 2026, 14:00", {}) }
}
