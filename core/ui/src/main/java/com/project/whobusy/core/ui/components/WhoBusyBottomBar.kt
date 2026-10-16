package com.project.whobusy.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

data class UiDestination(
    val title: String,
    val icon: UiIcon,
)

@Composable
fun WhoBusyBottomBar(
    items: List<UiDestination>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val shape = RoundedCornerShape(25.dp)
    Surface(
        modifier = modifier.height(64.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = shape,
    ) {
        BoxWithConstraints {
            val cellWidth = maxWidth / items.size
            val indicatorX by animateDpAsState(
                targetValue = cellWidth * selected.coerceIn(items.indices),
                label = "Navigation indicator",
            )
            Box(
                modifier =
                    Modifier
                        .offset(x = indicatorX)
                        .width(cellWidth)
                        .fillMaxHeight()
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            )
            Row(Modifier.fillMaxSize().selectableGroup()) {
                items.forEachIndexed { index, item ->
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(shape)
                                .selectable(
                                    selected = index == selected,
                                    role = Role.Tab,
                                    onClick = { if (index != selected) onSelect(index) },
                                ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    ) {
                        WhoBusyIcon(item.icon, tint = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Вкладки", showBackground = true, widthDp = 320)
@Preview(name = "Крупный шрифт", showBackground = true, widthDp = 320, fontScale = 1.5f)
@Composable
private fun BottomBarPreview() {
    WhoBusyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val items =
                listOf(
                    UiDestination("Календарь", UiIcon.Calendar),
                    UiDestination("Календари", UiIcon.People),
                    UiDestination("Профиль", UiIcon.Profile),
                )
            items.indices.forEach { selected ->
                WhoBusyBottomBar(items = items, selected = selected, onSelect = {})
            }
        }
    }
}
