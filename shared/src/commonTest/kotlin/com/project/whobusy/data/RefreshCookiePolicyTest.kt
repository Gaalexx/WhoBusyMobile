package com.project.whobusy.data

import com.project.whobusy.data.network.BrowserRefreshCookies
import com.project.whobusy.data.network.NativeRefreshCookies
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RefreshCookiePolicyTest {
    @Test
    fun nativePolicyReadsAndAttachesOnlyTheRefreshCredential() {
        val headers = headersOf(
            HttpHeaders.SetCookie,
            "refresh_token=test-only-refresh; Path=/api/auth; Max-Age=3600; HttpOnly; Secure; SameSite=Strict",
        )
        val token = NativeRefreshCookies.readRefresh(headers)
        val request = HttpRequestBuilder()
        NativeRefreshCookies.attachRefresh(request, token)
        assertEquals("refresh_token=test-only-refresh", request.headers[HttpHeaders.Cookie])
    }

    @Test
    fun nativePolicyRejectsMissingExpiredAndMalformedRefreshCredentials() {
        assertFailsWith<IllegalArgumentException> {
            NativeRefreshCookies.readRefresh(headersOf())
        }
        assertFailsWith<IllegalArgumentException> {
            NativeRefreshCookies.readRefresh(headersOf(HttpHeaders.SetCookie, "refresh_token=old; Max-Age=0"))
        }
        assertFailsWith<IllegalArgumentException> {
            NativeRefreshCookies.attachRefresh(HttpRequestBuilder(), "token; another=value")
        }
    }

    @Test
    fun browserPolicyNeverReturnsARefreshTokenEvenWhenATestHeaderIsAccessible() {
        val headers = headersOf(HttpHeaders.SetCookie, "refresh_token=must-not-enter-javascript; HttpOnly")
        assertNull(BrowserRefreshCookies.readRefresh(headers))
        val request = HttpRequestBuilder()
        BrowserRefreshCookies.attachRefresh(request, null)
        assertNull(request.headers[HttpHeaders.Cookie])
    }

    @Test
    fun browserPolicyRejectsAnyJavaScriptSideRefreshCredential() {
        assertFailsWith<IllegalArgumentException> {
            BrowserRefreshCookies.attachRefresh(HttpRequestBuilder(), "must-not-enter-javascript")
        }
    }
}
