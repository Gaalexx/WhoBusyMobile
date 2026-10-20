package com.project.whobusy.di

import com.project.whobusy.data.network.BrowserRefreshCookies
import com.project.whobusy.data.network.RefreshCookiePolicy
import com.project.whobusy.data.session.storage.BrowserRefreshTokenStore
import com.project.whobusy.data.session.storage.RefreshTokenStore
import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

@DependencyGraph(AppScope::class)
interface WebAppGraph : CalendarModule, ViewModelGraph {
    val httpClient: HttpClient
    val auth: AuthRepository
    val calendar: CalendarRepository
    val workspaces: WorkspaceRepository
    val authApi: AuthAPI

    @get:ApplicationScope
    val applicationScope: CoroutineScope

    @Provides
    fun provideRefreshCookiePolicy(): RefreshCookiePolicy = BrowserRefreshCookies

    @Provides
    fun provideRefreshTokenStore(): RefreshTokenStore = BrowserRefreshTokenStore

    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides configuration: AppConfiguration,
            @Provides client: HttpClient,
            @Provides @ApplicationScope scope: CoroutineScope,
        ): WebAppGraph
    }
}
