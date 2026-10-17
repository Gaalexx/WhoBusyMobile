package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun TimeGridLine(
    label: String,
    timeColumnWidth: Dp,
    modifier: Modifier = Modifier,
    lineY: Int = 0,
    hour: Int? = null,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    labelBackground: Color = Color.Transparent,
    thickness: Dp = 1.dp,
) {
    Layout(
        content = {
            Text(
                label,
                Modifier.background(labelBackground).padding(start = 8.dp),
                color = if (hour == null) color else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
            HorizontalDivider(
                modifier = if (hour == null) Modifier.testTag("time-grid-now") else Modifier.testTag("time-grid-line-$hour"),
                color = color,
                thickness = thickness,
            )
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val labelWidth = timeColumnWidth.roundToPx().coerceAtMost(constraints.maxWidth)
        val labelPlaceable = measurables[0].measure(Constraints.fixedWidth(labelWidth))
        val linePlaceable = measurables[1].measure(Constraints.fixedWidth(constraints.maxWidth - labelWidth))
        layout(constraints.maxWidth, constraints.maxHeight) {
            labelPlaceable.placeRelative(0, lineY - labelPlaceable.height / 2)
            linePlaceable.placeRelative(labelWidth, lineY - linePlaceable.height / 2)
        }
    }
}
