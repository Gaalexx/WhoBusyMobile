package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color.White)) {
        labels.forEachIndexed { index, label ->
            Column(Modifier.weight(1f).selectable(index == selected, role = Role.Tab, onClick = { onSelect(index) })) {
                Box(Modifier.fillMaxWidth().height(46.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(label, color = if (index == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium)
                }
                Box(Modifier.padding(horizontal = 19.dp).fillMaxWidth().height(2.dp)
                    .background(if (index == selected) MaterialTheme.colorScheme.primary else Color.Transparent))
            }
        }
    }
}

@Preview
@Composable
private fun TabsPreview() { WhoBusyTheme { WhoBusyTabs(listOf("День", "Неделя", "Месяц"), 0, {}) } }
