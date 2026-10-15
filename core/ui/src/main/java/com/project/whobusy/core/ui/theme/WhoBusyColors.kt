package com.project.whobusy.core.ui.theme

import androidx.compose.ui.graphics.Color

object WhoBusyColors {
    val Primary = Color(0xFF2481CC)
    val Header = Color(0xFF517FA4)
    val Background = Color(0xFFF1F3F5)
    val Text = Color(0xFF17212B)
    val SecondaryText = Color(0xFF72808D)
    val Divider = Color(0xFFE7EBEF)
    val BlueTint = Color(0xFFE9F3FC)
    val Purple = Color(0xFF6D4F9C)
    val PurpleTint = Color(0xFFF1EDF8)
    val Green = Color(0xFF357F59)
    val GreenTint = Color(0xFFECF5EE)
    val Error = Color(0xFFC84747)
    val ErrorTint = Color(0xFFFDEFF0)
}

fun calendarColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(WhoBusyColors.Primary)

fun calendarTint(color: Color): Color = when (color) {
    WhoBusyColors.Primary -> WhoBusyColors.BlueTint
    WhoBusyColors.Purple -> WhoBusyColors.PurpleTint
    WhoBusyColors.Green -> WhoBusyColors.GreenTint
    else -> color.copy(alpha = 0.1f)
}
