package com.project.whobusy.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val colors =
    lightColorScheme(
        primary = WhoBusyColors.Primary,
        onPrimary = Color.White,
        primaryContainer = WhoBusyColors.BlueTint,
        onPrimaryContainer = WhoBusyColors.Primary,
        background = WhoBusyColors.Background,
        onBackground = WhoBusyColors.Text,
        surface = Color.White,
        onSurface = WhoBusyColors.Text,
        onSurfaceVariant = WhoBusyColors.SecondaryText,
        outlineVariant = WhoBusyColors.Divider,
        error = WhoBusyColors.Error,
        errorContainer = WhoBusyColors.ErrorTint,
    )

private val typography =
    Typography(
        headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium),
        titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
        labelLarge = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    )

@Composable
fun WhoBusyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
