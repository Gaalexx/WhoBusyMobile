package com.project.whobusy.core.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.scrollToIndex
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.verticalScrollAxisRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyTimeGrid(
    hourHeight: Dp,
    timeColumnWidth: Dp,
    initialHour: Int,
    resetKey: Any,
    hourLabel: (Int) -> String,
    modifier: Modifier = Modifier,
    currentMinute: Int? = null,
    currentTimeLabel: String? = null,
    content: @Composable BoxWithConstraintsScope.(TimeGridViewport) -> Unit,
) {
    key(resetKey) {
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialHour.coerceIn(0, 23))
        val density = LocalDensity.current
        val hourHeightPx = with(density) { hourHeight.roundToPx() }
        val measuredHourHeight = with(density) { hourHeightPx.toDp() }
        val textMeasurer = rememberTextMeasurer()
        val labelHeight =
            textMeasurer.measure(AnnotatedString(hourLabel(0)), MaterialTheme.typography.bodySmall).size.height
        val insetPx = (labelHeight + 1) / 2
        val inset = with(density) { insetPx.toDp() }
        val bottomPadding = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val contentHeightPx = hourHeightPx * 24 + insetPx * 2 + with(density) { bottomPadding.roundToPx() }
        val scope = rememberCoroutineScope()
        val scrollPosition = {
            (listState.firstVisibleItemIndex * hourHeightPx + listState.firstVisibleItemScrollOffset).toFloat()
        }
        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .clipToBounds()
                // Handle drags above both the lazy hours and the event overlay with one scroll state.
                .scrollable(listState, Orientation.Vertical, reverseDirection = true)
                .semantics {
                    verticalScrollAxisRange = ScrollAxisRange(
                        value = scrollPosition,
                        maxValue = {
                            (contentHeightPx - listState.layoutInfo.viewportSize.height).coerceAtLeast(0).toFloat()
                        },
                    )
                    scrollBy { _, y ->
                        scope.launch { listState.scrollBy(y) }
                        true
                    }
                    scrollToIndex { index ->
                        if (index !in 0..24) false else {
                            scope.launch { listState.scrollToItem(index) }
                            true
                        }
                    }
                },
        ) {
            LazyColumn(
                state = listState,
                userScrollEnabled = false,
                contentPadding = PaddingValues(top = inset, bottom = bottomPadding),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(25, key = { it }, contentType = { "hour" }) { hour ->
                    TimeGridLine(
                        label = hourLabel(hour),
                        timeColumnWidth = timeColumnWidth,
                        modifier = Modifier.fillMaxWidth().height(if (hour == 24) inset else measuredHourHeight),
                        hour = hour,
                    )
                }
            }
            val viewport = TimeGridViewport(
                hourHeightPx = hourHeightPx,
                originY = insetPx - scrollPosition().toInt(),
                heightPx = constraints.maxHeight,
            )
            content(viewport)
            if (currentMinute != null && currentTimeLabel != null && viewport.isVisible(
                    currentMinute,
                    currentMinute + 1
                )
            ) {
                TimeGridLine(
                    label = currentTimeLabel,
                    timeColumnWidth = timeColumnWidth,
                    modifier = Modifier.fillMaxSize(),
                    lineY = viewport.minuteY(currentMinute),
                    color = MaterialTheme.colorScheme.error,
                    labelBackground = MaterialTheme.colorScheme.surface,
                    thickness = 2.dp,
                )
            }
        }
    }
}

@Preview(name = "Часовая сетка и границы событий", widthDp = 390, heightDp = 500, showBackground = true)
@Composable
private fun TimeGridPreview() {
    WhoBusyTheme {
        WhoBusyTimeGrid(
            hourHeight = 64.dp,
            timeColumnWidth = 60.dp,
            initialHour = 9,
            resetKey = Unit,
            hourLabel = { "${it.toString().padStart(2, '0')}:00" },
            currentMinute = 10 * 60 + 30,
            currentTimeLabel = "10:30",
        ) { viewport ->
            WhoBusyTimeGridItem(viewport, 9 * 60, 10 * 60, left = 64.dp, width = maxWidth - 76.dp) {
                WhoBusyEventCard(
                    "Встреча",
                    "09:00–10:00",
                    WhoBusyColors.Primary,
                    {},
                    Modifier.fillMaxSize(),
                    compact = true
                )
            }
            WhoBusyTimeGridItem(viewport, 10 * 60, 18 * 60, left = 64.dp, width = maxWidth - 76.dp) {
                WhoBusyEventCard("Длинный план", "10:00–18:00", WhoBusyColors.Primary, {}, Modifier.fillMaxSize())
            }
        }
    }
}
