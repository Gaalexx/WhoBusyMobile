package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyPersonRow(
    name: String, subtitle: String, color: Color, checked: Boolean,
    onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true,
) {
    Row(Modifier.fillMaxWidth().background(Color.White)
        .toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
        .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        WhoBusyAvatar(name, color)
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Checkbox(checked, onCheckedChange = null, enabled = enabled)
    }
}

@Preview
@Composable
private fun PersonRowPreview() {
    WhoBusyTheme { WhoBusyPersonRow("Анна Смирнова", "Календарь доступен", Color(0xFF6D4F9C), true, {}) }
}
