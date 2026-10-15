package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyDateCell(
    day: String, description: String, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier = Modifier, weekday: String? = null, muted: Boolean = false,
    eventColors: List<Color> = emptyList(),
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.height(if (weekday == null) 48.dp else 52.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected && weekday != null) colors.primary else Color.Transparent)
            .semantics { contentDescription = description }
            .selectable(selected, role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        weekday?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = if (selected) Color.White else colors.onSurfaceVariant) }
        Box(Modifier.size(36.dp).background(if (selected && weekday == null) colors.primary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center) {
            Text(day, color = if (selected) Color.White else if (muted) colors.onSurfaceVariant else colors.onSurface,
                style = MaterialTheme.typography.bodyLarge)
        }
        if (weekday == null) Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            eventColors.take(3).forEach { Box(Modifier.size(4.dp).background(it, CircleShape)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateCellPreview() {
    WhoBusyTheme { Row { WhoBusyDateCell("15", "15 октября", true, {}, Modifier.width(52.dp), weekday = "ЧТ")
        WhoBusyDateCell("16", "16 октября", false, {}, Modifier.width(52.dp), eventColors = listOf(Color.Blue)) } }
}
