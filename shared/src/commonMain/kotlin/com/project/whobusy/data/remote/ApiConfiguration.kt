package com.project.whobusy.data.remote

import io.ktor.http.URLProtocol
import io.ktor.http.Url

data class ApiConfiguration(
    val baseUrl: String,
) {
    fun endpoint(path: String): String {
        require(
            baseUrl.startsWith("https://") && baseUrl.none(Char::isWhitespace) &&
                baseUrl.removePrefix("https://").takeWhile { it !in "/?#" }.isNotEmpty(),
        ) {
            "Set whoBusy.apiBaseUrl to the HTTPS API base URL"
        }
        val url = Url(baseUrl)
        require(url.protocol == URLProtocol.HTTPS && url.host.isNotBlank()) {
            "Set whoBusy.apiBaseUrl to the HTTPS API base URL"
        }
        require('?' !in baseUrl && '#' !in baseUrl && url.user == null && url.password == null) {
            "API base URL must not contain credentials, query or fragment"
        }
        return baseUrl.trimEnd('/') + "/" + path.trimStart('/')
    }
}
