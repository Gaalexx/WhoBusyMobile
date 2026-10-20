package com.project.whobusy.di

import io.ktor.http.URLProtocol
import io.ktor.http.Url

internal fun webAppConfiguration(
    origin: String = browserOrigin(),
    useMockData: Boolean = WebBuildConfiguration.useMockData,
    apiBaseUrl: String = WebBuildConfiguration.apiBaseUrl,
): AppConfiguration {
    val location = Url(origin)
    val localDevelopment = location.protocol == URLProtocol.HTTP &&
        location.host.lowercase() in setOf("localhost", "127.0.0.1", "::1", "[::1]")
    return AppConfiguration(
        useMockData = useMockData,
        apiBaseUrl = apiBaseUrl.ifBlank { "${origin.trimEnd('/')}/api/" },
        allowLocalhostHttp = localDevelopment,
    )
}

internal expect fun browserOrigin(): String
