package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

enum class ButtonStyle { Primary, Secondary, Destructive }

@Composable
fun WhoBusyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val background =
        when (style) {
            ButtonStyle.Primary -> colors.primary
            ButtonStyle.Secondary -> colors.primaryContainer
            ButtonStyle.Destructive -> colors.errorContainer
        }
    val foreground =
        when (style) {
            ButtonStyle.Primary -> colors.onPrimary
            ButtonStyle.Secondary -> colors.primary
            ButtonStyle.Destructive -> colors.error
        }
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = foreground),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
        }
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    WhoBusyTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ButtonStyle.entries.forEach { WhoBusyButton("Продолжить", {}, style = it) }
            WhoBusyButton("Загрузка", {}, loading = true)
            WhoBusyButton("Недоступно", {}, enabled = false)
        }
    }
}
