package com.project.whobusy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.navigation.AppNavHost
import com.project.whobusy.presentation.session.ui.SessionRestoreScreen
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun WhoBusyApp(
    viewModel: AppViewModel = metroViewModel(),
    onSessionChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SideEffect {
        onSessionChanged(state.user != null)
    }
    WhoBusyTheme {
        if (state.startup == AppStartup.Ready) {
            AppNavHost(state.user?.id)
        } else {
            SessionRestoreScreen(
                loading = state.startup == AppStartup.Loading,
                onRetry = { viewModel.onEvent(AppCommand.RetryRestore) },
                onSignIn = { viewModel.onEvent(AppCommand.SignInAgain) },
            )
        }
    }
}
