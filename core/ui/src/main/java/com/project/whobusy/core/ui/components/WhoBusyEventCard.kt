package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.*

@Composable
fun WhoBusyEventCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(calendarTint(color)).clickable(onClick = onClick)) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(color))
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = color, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = color, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview() {
    WhoBusyTheme {
        WhoBusyEventCard("Синхронизация команды", "09:00–10:00 · Вы · Занят", WhoBusyColors.Primary, {}, Modifier.fillMaxWidth().height(68.dp))
    }
}
