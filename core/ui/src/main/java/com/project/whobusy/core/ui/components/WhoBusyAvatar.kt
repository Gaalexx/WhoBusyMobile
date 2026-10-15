package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyAvatar(name: String, color: Color, size: Dp = 44.dp) {
    val initials = name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString("")
    Box(Modifier.size(size).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(initials, color = Color.White)
    }
}

@Preview
@Composable
private fun AvatarPreview() {
    WhoBusyTheme { WhoBusyAvatar("Анна Смирнова", Color(0xFF6D4F9C)) }
}
