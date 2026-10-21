package com.project.whobusy.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.app_name
import com.project.whobusy.resources.whobusy_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyLogo(modifier: Modifier = Modifier.size(104.dp)) {
    Image(
        painter = painterResource(Res.drawable.whobusy_logo),
        contentDescription = stringResource(Res.string.app_name),
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true)
@Composable
private fun LogoPreview() {
    WhoBusyTheme { WhoBusyLogo() }
}
