package com.project.whobusy.data.remote

import io.ktor.http.URLProtocol
import io.ktor.http.Url

data class ApiConfiguration(
    val baseUrl: String,
    val allowLocalhostHttp: Boolean = false,
) {
    fun endpoint(path: String): String {
        require(
            (baseUrl.startsWith("https://") || allowLocalhostHttp && baseUrl.startsWith("http://")) &&
                baseUrl.none(Char::isWhitespace) &&
                baseUrl.substringAfter("://").takeWhile { it !in "/?#" }.isNotEmpty(),
        ) {
            "Set whoBusy.apiBaseUrl to the HTTPS API base URL"
        }
        val url = Url(baseUrl)
        val localhostHttp = allowLocalhostHttp && url.protocol == URLProtocol.HTTP &&
            url.host.lowercase() in setOf("localhost", "127.0.0.1", "::1", "[::1]")
        require((url.protocol == URLProtocol.HTTPS || localhostHttp) && url.host.isNotBlank()) {
            "Set whoBusy.apiBaseUrl to the HTTPS API base URL"
        }
        require('?' !in baseUrl && '#' !in baseUrl && url.user == null && url.password == null) {
            "API base URL must not contain credentials, query or fragment"
        }
        return baseUrl.trimEnd('/') + "/" + path.trimStart('/')
    }
}
