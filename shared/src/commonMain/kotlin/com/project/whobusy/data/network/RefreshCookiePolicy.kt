package com.project.whobusy.data.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.parseServerSetCookieHeader

interface RefreshCookiePolicy {
    val browserManaged: Boolean

    fun readRefresh(headers: Headers): String?

    fun attachRefresh(request: HttpRequestBuilder, token: String?)
}

object NativeRefreshCookies : RefreshCookiePolicy {
    override val browserManaged: Boolean = false

    override fun readRefresh(headers: Headers): String {
        val cookie = headers.getAll(HttpHeaders.SetCookie).orEmpty()
            .map(::parseServerSetCookieHeader).lastOrNull { it.name == REFRESH_COOKIE }
        require(cookie != null && cookie.value.isNotBlank() && cookie.maxAge != 0) { "Refresh cookie is missing" }
        return cookie.value
    }

    override fun attachRefresh(request: HttpRequestBuilder, token: String?) {
        request.headers.append(HttpHeaders.Cookie, refreshCookie(requireNotNull(token)))
    }

    fun refreshCookie(token: String): String {
        require(token.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid refresh token format" }
        return "$REFRESH_COOKIE=$token"
    }

    const val REFRESH_COOKIE = "refresh_token"
}

object BrowserRefreshCookies : RefreshCookiePolicy {
    override val browserManaged: Boolean = true

    override fun readRefresh(headers: Headers): String? = null

    override fun attachRefresh(request: HttpRequestBuilder, token: String?) {
        require(token == null) { "Browser refresh credentials must remain in HttpOnly cookies" }
    }
}
