package com.project.whobusy.presentation.spaces.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_role_actor
import com.project.whobusy.resources.spaces_role_actor_body
import com.project.whobusy.resources.spaces_role_admin
import com.project.whobusy.resources.spaces_role_admin_body
import com.project.whobusy.resources.spaces_role_moderator
import com.project.whobusy.resources.spaces_role_moderator_body
import com.project.whobusy.resources.spaces_role_visitor
import com.project.whobusy.resources.spaces_role_visitor_body
import org.jetbrains.compose.resources.stringResource

@Composable
fun spaceRoleTitle(role: SpaceRole): String =
    stringResource(
        when (role) {
            SpaceRole.Admin -> Res.string.spaces_role_admin
            SpaceRole.Moderator -> Res.string.spaces_role_moderator
            SpaceRole.Actor -> Res.string.spaces_role_actor
            SpaceRole.Visitor -> Res.string.spaces_role_visitor
        },
    )

@Composable
internal fun spaceRoleDescription(role: SpaceRole): String =
    stringResource(
        when (role) {
            SpaceRole.Admin -> Res.string.spaces_role_admin_body
            SpaceRole.Moderator -> Res.string.spaces_role_moderator_body
            SpaceRole.Actor -> Res.string.spaces_role_actor_body
            SpaceRole.Visitor -> Res.string.spaces_role_visitor_body
        },
    )

fun spaceColor(id: Int): Color =
    when ((id - 1).mod(3)) {
        0 -> WhoBusyColors.Primary
        1 -> WhoBusyColors.Purple
        else -> WhoBusyColors.Green
    }
