package com.project.whobusy.presentation.spaces.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.project.whobusy.R
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.domain.model.SpaceRole

@Composable
fun spaceRoleTitle(role: SpaceRole): String =
    stringResource(
        when (role) {
            SpaceRole.Admin -> R.string.spaces_role_admin
            SpaceRole.Moderator -> R.string.spaces_role_moderator
            SpaceRole.Actor -> R.string.spaces_role_actor
            SpaceRole.Visitor -> R.string.spaces_role_visitor
        },
    )

@Composable
internal fun spaceRoleDescription(role: SpaceRole): String =
    stringResource(
        when (role) {
            SpaceRole.Admin -> R.string.spaces_role_admin_body
            SpaceRole.Moderator -> R.string.spaces_role_moderator_body
            SpaceRole.Actor -> R.string.spaces_role_actor_body
            SpaceRole.Visitor -> R.string.spaces_role_visitor_body
        },
    )

fun spaceColor(id: Int): Color =
    when ((id - 1).mod(3)) {
        0 -> WhoBusyColors.Primary
        1 -> WhoBusyColors.Purple
        else -> WhoBusyColors.Green
    }
