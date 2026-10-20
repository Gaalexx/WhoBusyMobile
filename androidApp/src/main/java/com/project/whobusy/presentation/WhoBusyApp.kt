package com.project.whobusy.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.navigation.AppNavHost
import com.project.whobusy.presentation.session.ui.SessionRestoreScreen
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun WhoBusyApp(viewModel: AppViewModel = metroViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val view = LocalView.current
    SideEffect {
        val activity = view.context as? Activity
        if (activity != null) {
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = state.user == null
                isAppearanceLightNavigationBars = true
            }
        }
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
