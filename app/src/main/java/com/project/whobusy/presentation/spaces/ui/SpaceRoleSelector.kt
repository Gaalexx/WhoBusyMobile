package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusySelectionMark
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.SpaceRole

@Composable
internal fun SpaceRoleSelector(
    selected: SpaceRole,
    onSelect: (SpaceRole) -> Unit,
    allowedRoles: Set<SpaceRole> = SpaceRole.entries.toSet(),
    enabled: Boolean = true,
) {
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SpaceRole.entries.forEach { role ->
            val available = enabled && role in allowedRoles
            WhoBusyListRow(
                title = spaceRoleTitle(role),
                subtitle = spaceRoleDescription(role),
                trailing = { WhoBusySelectionMark(role == selected) },
                modifier =
                    Modifier
                        .alpha(
                            if (available) 1f else 0.45f,
                        ).selectable(
                            role == selected, enabled = available, role = Role.RadioButton,
                            onClick = {
                                onSelect(role)
                            }
                        ),
            )
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun RolesPreview() {
    WhoBusyTheme { SpaceRoleSelector(SpaceRole.Actor, {}) }
}
