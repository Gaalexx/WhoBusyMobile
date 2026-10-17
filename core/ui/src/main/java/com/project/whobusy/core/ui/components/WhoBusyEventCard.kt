package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarTint

@Composable
fun WhoBusyEventCard(
    title: String,
    subtitle: String?,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    compact: Boolean = false,
    showText: Boolean = true,
) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(calendarTint(color))
            .clickable(onClick = onClick)
            .semantics { if (!showText) contentDescription = title },
    ) {
        if (showText) Column(
            Modifier.padding(horizontal = 10.dp, vertical = if (compact) 2.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 0.dp else 4.dp),
        ) {
            Text(
                title,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let {
                Text(
                    it,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            detail?.let {
                Text(it, color = color, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview() {
    WhoBusyTheme {
        WhoBusyEventCard(
            "Синхронизация команды",
            stringResource(
                R.string.ui_preview_event_subtitle,
            ),
            WhoBusyColors.Primary,
            {
            },
            Modifier.fillMaxWidth().height(68.dp),
        )
    }
}

@Preview(name = "Короткий план", showBackground = true)
@Composable
private fun CompactEventCardPreview() {
    WhoBusyTheme {
        WhoBusyEventCard(
            "Обсуждение релиза",
            stringResource(R.string.ui_preview_event_subtitle),
            WhoBusyColors.Primary,
            {},
            Modifier.fillMaxWidth().height(62.dp),
            detail = "Momentum",
            compact = true,
        )
    }
}

@Preview(name = "Минимальный временной блок", showBackground = true)
@Composable
private fun MinimalEventCardPreview() {
    WhoBusyTheme {
        WhoBusyEventCard(
            title = "Короткий план",
            subtitle = null,
            color = WhoBusyColors.Primary,
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(8.dp),
            showText = false,
        )
    }
}
