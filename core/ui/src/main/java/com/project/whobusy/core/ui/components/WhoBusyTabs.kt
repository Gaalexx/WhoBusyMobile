package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyTabs(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(Modifier.fillMaxWidth().background(Color.White)) {
        labels.forEachIndexed { index, label ->
            Column(Modifier.weight(1f).selectable(index == selected, role = Role.Tab, onClick = { onSelect(index) })) {
                Box(Modifier.fillMaxWidth().height(46.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        label,
                        color =
                            if (index ==
                                selected
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Box(
                    Modifier
                        .padding(horizontal = 19.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (index == selected) MaterialTheme.colorScheme.primary else Color.Transparent),
                )
            }
        }
    }
}

@Preview
@Composable
private fun TabsPreview() {
    WhoBusyTheme {
        WhoBusyTabs(
            listOf(
                stringResource(R.string.ui_calendar_mode_day),
                stringResource(R.string.ui_calendar_mode_week),
                stringResource(R.string.ui_calendar_mode_month),
            ),
            0,
            {
            },
        )
    }
}
