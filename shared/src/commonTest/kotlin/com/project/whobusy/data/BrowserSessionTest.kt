package com.project.whobusy.data

import com.project.whobusy.data.network.ApiHttpException
import com.project.whobusy.data.network.AuthenticatedHttpClient
import com.project.whobusy.data.network.BrowserRefreshCookies
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.RemoteSessionManager
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.data.session.storage.BrowserRefreshTokenStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.RegistrationDraft
import com.project.whobusy.time.Clock
import com.project.whobusy.time.Instant
import com.project.whobusy.time.ZoneOffset
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrowserSessionTest {
    @Test
    fun loginAndRegistrationAcceptInaccessibleHttpOnlyCookiesWithoutStoringRefresh() = runTest {
        val fixture = BrowserFixture(backgroundScope)
        try {
            fixture.manager.login(" DEMO@WHOBUSY.TEST ", "test-password")
            assertEquals("test-only-access", fixture.session.getToken())
            assertEquals("demo", fixture.session.getUser()?.login)
            fixture.manager.register(RegistrationDraft("new-user", "Имя", "Фамилия", "test-password"))
            assertEquals(listOf("login", "me", "register", "me"), fixture.requests.map { it.url.segments.last() })
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    @Test
    fun reloadRestoresByBrowserCookieWithoutAnyJavaScriptRefreshCredential() = runTest {
        val fixture = BrowserFixture(backgroundScope)
        try {
            fixture.manager.restore()
            assertEquals(listOf("refresh", "me"), fixture.requests.map { it.url.segments.last() })
            assertEquals("test-only-access", fixture.session.getToken())
            assertNotNull(fixture.session.getUser())
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    @Test
    fun missingOrRevokedCookieRestoresGuestWithoutAccessingJavaScriptStorage() = runTest {
        val fixture = BrowserFixture(backgroundScope) { request ->
            if (request.url.encodedPath.endsWith("/refresh")) Reply(status = HttpStatusCode.Unauthorized) else null
        }
        try {
            fixture.manager.restore()
            assertEquals(1, fixture.requests.size)
            assertNull(fixture.session.getToken())
            assertNull(fixture.session.getUser())
        } finally {
            fixture.close()
        }
    }

    @Test
    fun profilePatchUsesBearerAndLetsTheBrowserAttachItsOwnCookie() = runTest {
        val fixture = BrowserFixture(backgroundScope)
        try {
            fixture.manager.login("demo", "test-password")
            fixture.authenticated.request("auth/me", HttpMethod.Patch)
            val request = fixture.requests.last()
            assertEquals(HttpMethod.Patch, request.method)
            assertEquals("Bearer test-only-access", request.headers[HttpHeaders.Authorization])
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    @Test
    fun concurrentUnauthorizedRequestsShareBrowserCookieRefresh() = runTest {
        val fixture = BrowserFixture(backgroundScope)
        try {
            fixture.manager.login("demo", "test-password")
            val allStarted = CompletableDeferred<Unit>()
            var firstAttempts = 0
            val operations = List(5) {
                async {
                    var attempts = 0
                    fixture.manager.authorized {
                        if (attempts++ == 0) {
                            firstAttempts++
                            if (firstAttempts == 5) allStarted.complete(Unit)
                            allStarted.await()
                            throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                        }
                        "ok"
                    }
                }
            }
            assertEquals(List(5) { "ok" }, operations.awaitAll())
            assertEquals(1, fixture.requests.count { it.url.encodedPath.endsWith("/refresh") })
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    @Test
    fun cookieIdentityChangeDoesNotRetryThePreviousUsersMutation() = runTest {
        var anotherTabChangedIdentity = false
        val fixture = BrowserFixture(backgroundScope) { request ->
            when {
                anotherTabChangedIdentity && request.url.encodedPath.endsWith("/refresh") -> Reply(OTHER_TOKEN_JSON)
                anotherTabChangedIdentity && request.url.encodedPath.endsWith("/me") -> Reply(OTHER_USER_JSON)
                else -> null
            }
        }
        try {
            fixture.manager.login("demo", "test-password")
            anotherTabChangedIdentity = true
            val attemptedTokens = mutableListOf<String>()
            assertFailsWith<CancellationException> {
                fixture.repositoryOperation {
                    fixture.manager.authorized {
                        attemptedTokens += it
                        throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                    }
                }
            }
            assertEquals(listOf("test-only-access"), attemptedTokens)
            assertEquals(2, fixture.session.getUser()?.id)
            assertEquals("test-only-other-access", fixture.session.getToken())
            fixture.manager.authorized { assertEquals("test-only-other-access", it) }
            assertTrue(fixture.requests.none { it.url.encodedPath.endsWith("/logout") })
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    @Test
    fun cookieIdentityChangeDuringExpiryRenewalRejectsTheOldOperationBeforeItRuns() = runTest {
        var anotherTabChangedIdentity = false
        val fixture = BrowserFixture(backgroundScope) { request ->
            when {
                request.url.encodedPath.endsWith("/login") -> Reply(TOKEN_JSON.replace("900", "1"))
                anotherTabChangedIdentity && request.url.encodedPath.endsWith("/refresh") -> Reply(OTHER_TOKEN_JSON)
                anotherTabChangedIdentity && request.url.encodedPath.endsWith("/me") -> Reply(OTHER_USER_JSON)
                else -> null
            }
        }
        try {
            fixture.manager.login("demo", "test-password")
            anotherTabChangedIdentity = true
            var operationRan = false
            assertFailsWith<CancellationException> {
                fixture.repositoryOperation {
                    fixture.manager.authorized { operationRan = true }
                }
            }
            assertFalse(operationRan)
            assertEquals(2, fixture.session.getUser()?.id)
            assertEquals("test-only-other-access", fixture.session.getToken())
            assertTrue(fixture.requests.none { it.url.encodedPath.endsWith("/logout") })
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    @Test
    fun staleSuccessAndUnauthorizedLeasesCannotLogOutTheReplacementBrowserSession() = runTest {
        for ((retry, unauthorized) in listOf(false to false, false to true, true to false, true to true)) {
            var anotherTabChangedIdentity = false
            val requestStarted = CompletableDeferred<Unit>()
            val completeRequest = CompletableDeferred<Unit>()
            val fixture = BrowserFixture(backgroundScope) { request ->
                when {
                    anotherTabChangedIdentity && request.url.encodedPath.endsWith("/refresh") -> Reply(OTHER_TOKEN_JSON)
                    anotherTabChangedIdentity && request.url.encodedPath.endsWith("/me") -> Reply(OTHER_USER_JSON)
                    else -> null
                }
            }
            try {
                fixture.manager.login("demo", "test-password")
                val oldRequest = async {
                    runCatching {
                        fixture.repositoryOperation {
                            var attempts = 0
                            fixture.manager.authorized {
                                if (attempts++ == 0 && retry) {
                                    throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                                }
                                requestStarted.complete(Unit)
                                completeRequest.await()
                                if (unauthorized) throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                                "old-session-data"
                            }
                        }
                    }.exceptionOrNull()
                }
                requestStarted.await()
                anotherTabChangedIdentity = true
                assertFailsWith<CancellationException> {
                    fixture.repositoryOperation {
                        fixture.manager.authorized<Unit> {
                            throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                        }
                    }
                }
                completeRequest.complete(Unit)
                assertTrue(oldRequest.await() is CancellationException)
                assertEquals(2, fixture.session.getUser()?.id)
                assertEquals("test-only-other-access", fixture.session.getToken())
                assertTrue(fixture.requests.none { it.url.encodedPath.endsWith("/logout") })
                fixture.assertNoManualCookies()
            } finally {
                completeRequest.complete(Unit)
                fixture.close()
            }
        }
    }

    @Test
    fun supersededCredentialsOfTheSameUserReturnUiFailureWithoutCancellingOrLoggingOut() = runTest {
        val requestStarted = CompletableDeferred<Unit>()
        val completeRequest = CompletableDeferred<Unit>()
        val fixture = BrowserFixture(backgroundScope)
        try {
            fixture.manager.login("demo", "test-password")
            var isSaving = true
            val oldRequest = async {
                runCatching {
                    repositoryResult {
                        var attempts = 0
                        fixture.manager.authorized {
                            if (attempts++ == 0) throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                            requestStarted.complete(Unit)
                            completeRequest.await()
                            throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
                        }
                    }.onFailure { failure ->
                        if (failure is AuthenticationException) fixture.manager.logout()
                        isSaving = false
                    }.getOrThrow()
                }.exceptionOrNull()
            }
            requestStarted.await()
            var newerAttempts = 0
            fixture.manager.authorized {
                if (newerAttempts++ == 0) throw ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")
            }
            completeRequest.complete(Unit)
            val failure = oldRequest.await()
            assertTrue(failure is ApiHttpException)
            assertEquals(HttpStatusCode.Unauthorized, failure.status)
            assertFalse(isSaving)
            assertEquals(1, fixture.session.getUser()?.id)
            assertEquals("test-only-access", fixture.session.getToken())
            assertTrue(fixture.requests.none { it.url.encodedPath.endsWith("/logout") })
            fixture.assertNoManualCookies()
        } finally {
            completeRequest.complete(Unit)
            fixture.close()
        }
    }

    @Test
    fun logoutWaitsForServerCookieDeletionBeforePublishingGuest() = runTest {
        val started = CompletableDeferred<Unit>()
        val complete = CompletableDeferred<Unit>()
        val fixture = BrowserFixture(backgroundScope) { request ->
            if (request.url.encodedPath.endsWith("/logout")) {
                started.complete(Unit)
                complete.await()
            }
            null
        }
        try {
            fixture.manager.login("demo", "test-password")
            val logout = async { fixture.manager.logout() }
            started.await()
            assertFalse(logout.isCompleted)
            assertNotNull(fixture.session.getUser())
            complete.complete(Unit)
            logout.await()
            assertNull(fixture.session.getToken())
            assertNull(fixture.session.getUser())
            fixture.assertNoManualCookies()
        } finally {
            complete.complete(Unit)
            fixture.close()
        }
    }

    @Test
    fun failedLogoutRetainsTheSessionBecauseJavaScriptCannotClearHttpOnlyCookie() = runTest {
        val fixture = BrowserFixture(backgroundScope) { request ->
            if (request.url.encodedPath.endsWith("/logout")) Reply(status = HttpStatusCode.ServiceUnavailable) else null
        }
        try {
            fixture.manager.login("demo", "test-password")
            assertFailsWith<ApiHttpException> { fixture.manager.logout() }
            assertEquals("test-only-access", fixture.session.getToken())
            assertNotNull(fixture.session.getUser())
        } finally {
            fixture.close()
        }
    }

    @Test
    fun callerCancellationDoesNotInterruptConfirmedBrowserLogout() = runTest {
        val started = CompletableDeferred<Unit>()
        val complete = CompletableDeferred<Unit>()
        val fixture = BrowserFixture(backgroundScope) { request ->
            if (request.url.encodedPath.endsWith("/logout")) {
                started.complete(Unit)
                complete.await()
            }
            null
        }
        try {
            fixture.manager.login("demo", "test-password")
            val logout = async { fixture.manager.logout() }
            started.await()
            logout.cancel()
            complete.complete(Unit)
            logout.join()
            assertTrue(logout.isCancelled)
            assertNull(fixture.session.getToken())
            assertNull(fixture.session.getUser())
        } finally {
            complete.complete(Unit)
            fixture.close()
        }
    }

    @Test
    fun logoutAllUsesBearerAndClearsMemoryAfterServerConfirmation() = runTest {
        val fixture = BrowserFixture(backgroundScope)
        try {
            fixture.manager.login("demo", "test-password")
            fixture.manager.logoutAll()
            assertEquals("/api/auth/logout-all", fixture.requests.last().url.encodedPath)
            assertEquals("Bearer test-only-access", fixture.requests.last().headers[HttpHeaders.Authorization])
            assertNull(fixture.session.getToken())
            assertNull(fixture.session.getUser())
            fixture.assertNoManualCookies()
        } finally {
            fixture.close()
        }
    }

    private class BrowserFixture(
        scope: CoroutineScope,
        private val response: suspend (HttpRequestData) -> Reply? = { null },
    ) {
        val requests = mutableListOf<HttpRequestData>()
        val session = SessionStore()
        private val configuration = ApiConfiguration("https://calendar.example.test/api/")
        private val json = Json { ignoreUnknownKeys = true }
        private val client = HttpClient(MockEngine { request ->
            requests += request
            val reply = response(request) ?: when (request.url.segments.last()) {
                "login", "register", "refresh" -> Reply(TOKEN_JSON)
                "me" -> Reply(USER_JSON)
                else -> Reply(status = HttpStatusCode.NoContent)
            }
            respond(reply.body, reply.status, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            expectSuccess = false
            install(ContentNegotiation) { json(json) }
        }
        val manager = RemoteSessionManager(
            AuthAPI(client, configuration, BrowserRefreshCookies),
            session,
            BrowserRefreshTokenStore,
            configuration,
            json,
            Clock.fixed(Instant.parse("2026-10-20T09:00:00Z"), ZoneOffset.UTC),
            scope,
        )
        val authenticated = AuthenticatedHttpClient(client, configuration, manager)

        suspend fun <Value> repositoryOperation(block: suspend () -> Value): Value =
            repositoryResult(block).onFailure { failure ->
                if (failure is AuthenticationException) manager.logout()
            }.getOrThrow()

        fun assertNoManualCookies() {
            for (request in requests) {
                assertNull(request.headers[HttpHeaders.Cookie])
            }
        }

        fun close() {
            client.close()
        }
    }

    private data class Reply(val body: String = "{}", val status: HttpStatusCode = HttpStatusCode.OK)

    companion object {
        private const val TOKEN_JSON = """{"access_token":"test-only-access","token_type":"bearer","expires_in":900}"""
        private const val USER_JSON = """{"id":1,"login":"demo","email":null,"first_name":"Имя","last_name":"Фамилия","full_name":"Фамилия Имя","role":"user","color":"#4c5c9e","is_active":true}"""
        private const val OTHER_TOKEN_JSON = """{"access_token":"test-only-other-access","token_type":"bearer","expires_in":900}"""
        private const val OTHER_USER_JSON = """{"id":2,"login":"another-user","email":null,"first_name":"Другое","last_name":"Имя","full_name":"Другое Имя","role":"user","color":"#4c5c9e","is_active":true}"""
    }
}
