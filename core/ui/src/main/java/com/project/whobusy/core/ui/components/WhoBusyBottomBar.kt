package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

data class UiDestination(val title: String, val icon: UiIcon)

@Composable
fun WhoBusyBottomBar(items: List<UiDestination>, selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.background(Color.White)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().height(64.dp)) {
            items.forEachIndexed { index, item ->
                val tint = if (index == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(Modifier.weight(1f).fillMaxHeight().selectable(index == selected, role = Role.Tab, onClick = { onSelect(index) }),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    WhoBusyIcon(item.icon, tint = tint)
                    Spacer(Modifier.height(4.dp))
                    Text(item.title, color = tint, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Preview
@Composable
private fun BottomBarPreview() {
    WhoBusyTheme { WhoBusyBottomBar(listOf(UiDestination("Календарь", UiIcon.Calendar), UiDestination("Календари", UiIcon.People), UiDestination("Профиль", UiIcon.Profile)), 0, {}) }
}
