package com.project.whobusy.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object BrowserHttpClientFactory {
    fun create(json: Json): HttpClient = HttpClient(createBrowserEngine()) {
        expectSuccess = false
        followRedirects = false
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }
}

internal expect fun createBrowserEngine(): HttpClientEngine
