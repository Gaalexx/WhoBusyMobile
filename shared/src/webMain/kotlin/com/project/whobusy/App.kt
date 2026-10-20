package com.project.whobusy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.whobusy.data.network.BrowserHttpClientFactory
import com.project.whobusy.di.WebAppGraph
import com.project.whobusy.di.webAppConfiguration
import com.project.whobusy.presentation.WhoBusyApp
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.web_demo_notice
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    val configuration = remember { webAppConfiguration() }
    val graph = remember(configuration) {
        createGraphFactory<WebAppGraph.Factory>().create(
            configuration,
            BrowserHttpClientFactory.create(Json { ignoreUnknownKeys = true }),
            CoroutineScope(SupervisorJob() + Dispatchers.Main),
        )
    }
    DisposableEffect(graph) {
        onDispose {
            graph.applicationScope.cancel()
            graph.httpClient.close()
        }
    }
    CompositionLocalProvider(LocalMetroViewModelFactory provides graph.metroViewModelFactory) {
        Box(Modifier.fillMaxSize()) {
            WhoBusyApp(desktopLayout = true)
            if (configuration.useMockData) {
                Text(
                    text = stringResource(Res.string.web_demo_notice),
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .background(Color(0xFFFFF2C6))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = Color(0xFF5E4700),
                    fontSize = 11.sp,
                )
            }
        }
    }
}
