package com.project.whobusy.di

import com.project.whobusy.data.network.HttpClientFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

interface NetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
        }

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(json: Json): HttpClient = HttpClientFactory.create(json)
}
