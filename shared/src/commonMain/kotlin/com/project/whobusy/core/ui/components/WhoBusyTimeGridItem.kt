package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/** Keeps a continuous event visible even after its starting hour leaves the lazy list. */
@Composable
fun WhoBusyTimeGridItem(
    viewport: TimeGridViewport,
    startMinute: Int,
    endMinute: Int,
    left: Dp,
    width: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!viewport.isVisible(startMinute, endMinute)) return
    Layout(
        content = { Box(modifier, content = content) },
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        // A multi-hour card may be taller than the viewport. Measure its full duration,
        // then let the grid clip it rather than squeezing it into the visible height.
        val placeable = measurables.single().measure(
            Constraints.fixed(
                width.roundToPx().coerceAtLeast(1),
                // At low density a one-minute interval can be shorter than a pixel.
                viewport.eventHeight(startMinute, endMinute).coerceAtLeast(1),
            ),
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(left.roundToPx(), viewport.minuteY(startMinute))
        }
    }
}
