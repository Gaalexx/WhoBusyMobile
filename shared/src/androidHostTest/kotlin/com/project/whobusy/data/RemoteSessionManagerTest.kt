package com.project.whobusy.data

import com.project.whobusy.data.network.ApiHttpException
import com.project.whobusy.data.network.AuthenticatedHttpClient
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.session.RemoteSessionManager
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.data.session.storage.RefreshTokenStore
import com.project.whobusy.domain.model.AuthenticationException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.project.whobusy.time.Clock
import com.project.whobusy.time.Instant
import com.project.whobusy.time.ZoneOffset
import com.project.whobusy.time.from
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

class RemoteSessionManagerTest {
    @Test(timeout = 10_000)
    fun simultaneousUnauthorizedRequestsShareOneRefresh() = runBlocking {
        assertConcurrentRefreshIsCoalesced(sameAccessToken = false)
    }

    @Test(timeout = 10_000)
    fun simultaneousUnauthorizedRequestsShareRefreshEvenWhenServerReusesTheAccessToken() = runBlocking {
        assertConcurrentRefreshIsCoalesced(sameAccessToken = true)
    }

    @Test(timeout = 10_000)
    fun forbiddenResponseDoesNotRefreshOrInvalidateTheSession() = runBlocking {
        SessionFixture().use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val persisted = store.value
            var attempts = 0

            val failure = runCatching {
                manager.authorized<Unit> {
                    attempts++
                    throw ApiHttpException(HttpStatusCode.Forbidden, "Forbidden")
                }
            }.exceptionOrNull()

            assertEquals(HttpStatusCode.Forbidden, (failure as ApiHttpException).status)
            assertEquals(1, attempts)
            assertEquals(0, fixture.refreshCount())
            assertTrue("A permission failure must retain the refresh credential", persisted == store.value)
            assertNotNull(session.getUser())
        }
    }

    @Test(timeout = 10_000)
    fun rejectedRefreshClearsBothPersistedAndInMemoryCredentials() = runBlocking {
        SessionFixture { request ->
            if (request.url.encodedPath.endsWith("/refresh")) Reply(status = HttpStatusCode.Unauthorized) else null
        }.use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")

            val failure = runCatching {
                manager.authorized<Unit> { throw unauthorized() }
            }.exceptionOrNull()

            assertTrue(failure is AuthenticationException)
            assertEquals(1, fixture.refreshCount())
            assertNull(store.value)
            assertNull(session.getToken())
            assertNull(session.getUser())
        }
    }

    @Test(timeout = 10_000)
    fun temporaryRefreshFailurePreservesTheExistingSessionForRetry() = runBlocking {
        SessionFixture { request ->
            if (request.url.encodedPath.endsWith("/refresh")) Reply(status = HttpStatusCode.ServiceUnavailable) else null
        }.use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val persisted = store.value
            val access = session.getToken()

            val failure = runCatching {
                manager.authorized<Unit> { throw unauthorized() }
            }.exceptionOrNull()

            assertEquals(HttpStatusCode.ServiceUnavailable, (failure as ApiHttpException).status)
            assertEquals(1, fixture.refreshCount())
            assertTrue("A server failure must retain the refresh credential", persisted == store.value)
            assertTrue("A server failure must retain the current access credential", access == session.getToken())
            assertNotNull(session.getUser())
        }
    }

    @Test(timeout = 10_000)
    fun secondUnauthorizedResponseInvalidatesAndNeverStartsAThirdAttempt() = runBlocking {
        SessionFixture().use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            var attempts = 0

            val failure = runCatching {
                manager.authorized<Unit> {
                    attempts++
                    throw unauthorized()
                }
            }.exceptionOrNull()

            assertTrue(failure is AuthenticationException)
            assertEquals(2, attempts)
            assertEquals(1, fixture.refreshCount())
            assertNull(store.value)
            assertNull(session.getUser())
            assertNull(session.getToken())
        }
    }

    @Test(timeout = 10_000)
    fun aNewManagerRestoresFromOnlyThePersistedRefreshAndRotatesIt() = runBlocking {
        SessionFixture().use { fixture ->
            val store = GatedRefreshTokenStore()
            val firstSession = SessionStore()
            fixture.manager(firstSession, store).login("demo", "test-password")
            val stored = fixture.json.parseToJsonElement(requireNotNull(store.value)).jsonObject

            assertEquals(setOf("apiBaseUrl", "token"), stored.keys)
            assertTrue(
                "Storage contains the refresh credential",
                stored.getValue("token").jsonPrimitive.content == INITIAL_REFRESH
            )
            assertFalse("Access credentials must never be persisted", store.value!!.contains(INITIAL_ACCESS))
            assertFalse("Passwords must never be persisted", store.value!!.contains("test-password"))

            val restoredSession = SessionStore()
            fixture.manager(restoredSession, store).restore()

            assertEquals(1, fixture.refreshCount())
            assertTrue(
                "Restore must use the stored refresh credential",
                fixture.refreshRequests().single().headers[HttpHeaders.Cookie] == AuthAPI.refreshCookie(INITIAL_REFRESH)
            )
            assertTrue("Restore publishes the new access credential", restoredSession.getToken() == ROTATED_ACCESS)
            assertEquals(1, restoredSession.getUser()?.id)
            assertTrue("Restore durably replaces the rotated refresh", storedRefresh(store.value) == ROTATED_REFRESH)
            val profileRequest = fixture.requests().last { it.url.encodedPath == "/api/auth/me" }
            assertTrue(
                "Restored profile uses the new access credential",
                profileRequest.headers[HttpHeaders.Authorization] == "Bearer $ROTATED_ACCESS"
            )
        }
    }

    @Test(timeout = 10_000)
    fun aRefreshBoundToAnotherApiOriginIsClearedWithoutAnyHttpRequest() = runBlocking {
        SessionFixture().use { fixture ->
            val store = GatedRefreshTokenStore(
                buildJsonObject {
                    put("apiBaseUrl", "https://another.example.test/api/")
                    put("token", INITIAL_REFRESH)
                }.toString(),
            )
            val session = SessionStore()

            fixture.manager(session, store).restore()

            assertTrue("Credentials from another origin must never be transmitted", fixture.requests().isEmpty())
            assertNull(store.value)
            assertNull(session.getUser())
            assertNull(session.getToken())
        }
    }

    @Test(timeout = 10_000)
    fun logoutWaitsForDurableClearBeforePublishingAndFinishesAfterCallerCancellation() = runBlocking {
        SessionFixture().use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val clearStarted = CompletableDeferred<Unit>()
            val allowClear = CompletableDeferred<Unit>()
            store.beforeClear = {
                clearStarted.complete(Unit)
                allowClear.await()
            }
            var returnedFromLogout = false
            val logout = async {
                manager.logout()
                returnedFromLogout = true
            }
            try {
                clearStarted.await()
                assertFalse("Logout must await durable invalidation", logout.isCompleted)
                assertNotNull("Session is not cleared before durable storage", session.getUser())
                assertNotNull(store.value)
                logout.cancel()
            } finally {
                allowClear.complete(Unit)
            }
            logout.join()

            assertTrue("Caller cancellation still propagates", logout.isCancelled)
            assertFalse("Cancelled logout cannot resume nonsuspending caller code", returnedFromLogout)
            assertNull("Cancellation must not retain a restorable refresh", store.value)
            assertNull(session.getUser())
            assertNull(session.getToken())
        }
    }

    @Test(timeout = 10_000)
    fun cancelledRestoreCommitsTheRotatedSessionWithoutReturningToTheCaller() = runBlocking {
        SessionFixture().use { fixture ->
            val store = GatedRefreshTokenStore()
            fixture.manager(SessionStore(), store).login("demo", "test-password")
            val session = SessionStore()
            val manager = fixture.manager(session, store)
            val writeStarted = CompletableDeferred<Unit>()
            val allowWrite = CompletableDeferred<Unit>()
            store.beforeWrite = {
                writeStarted.complete(Unit)
                allowWrite.await()
            }
            var returnedFromRestore = false
            val restoring = async {
                manager.restore()
                returnedFromRestore = true
            }
            try {
                writeStarted.await()
                assertNull("Restore publishes only after durable rotation", session.getUser())
                restoring.cancel()
            } finally {
                allowWrite.complete(Unit)
            }
            restoring.join()

            assertTrue("Restore propagates caller cancellation after committing", restoring.isCancelled)
            assertFalse("Cancelled restore cannot resume nonsuspending caller code", returnedFromRestore)
            assertEquals(1, fixture.refreshCount())
            assertEquals(ROTATED_REFRESH, storedRefresh(store.value))
            assertEquals(ROTATED_ACCESS, session.getToken())
            assertNotNull(session.getUser())
        }
    }

    @Test(timeout = 10_000)
    fun cancellingARequestDuringRefreshStillCommitsTheRotatedCredentials() = runBlocking {
        val refreshStarted = CompletableDeferred<Unit>()
        val allowRefreshResponse = CompletableDeferred<Unit>()
        SessionFixture { request ->
            if (request.url.encodedPath.endsWith("/refresh")) {
                refreshStarted.complete(Unit)
                allowRefreshResponse.await()
                Reply.tokens(ROTATED_ACCESS, ROTATED_REFRESH)
            } else null
        }.use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val writeStarted = CompletableDeferred<Unit>()
            val allowWrite = CompletableDeferred<Unit>()
            store.beforeWrite = {
                writeStarted.complete(Unit)
                allowWrite.await()
            }
            val request = async { manager.authorized<Unit> { throw unauthorized() } }
            try {
                refreshStarted.await()
                request.cancel()
                allowRefreshResponse.complete(Unit)
                writeStarted.await()
                assertTrue("Access changes only after persistence", session.getToken() == INITIAL_ACCESS)
                assertTrue(
                    "The old refresh remains until the durable commit",
                    storedRefresh(store.value) == INITIAL_REFRESH
                )
            } finally {
                allowRefreshResponse.complete(Unit)
                allowWrite.complete(Unit)
            }
            request.join()

            assertTrue("The original caller remains cancelled", request.isCancelled)
            assertEquals(1, fixture.refreshCount())
            assertTrue("Rotation must survive caller cancellation", storedRefresh(store.value) == ROTATED_REFRESH)
            assertTrue("The committed access and refresh must match", session.getToken() == ROTATED_ACCESS)
            assertNotNull(session.getUser())
        }
    }

    @Test(timeout = 10_000)
    fun retriedProfileOperationsReceiveAnAtomicAccessAndRefreshPair() = runBlocking {
        SessionFixture().use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val firstLeaseObtained = CompletableDeferred<Unit>()
            val allowFirstResponse = CompletableDeferred<Unit>()
            val pairs = mutableListOf<Pair<String, String?>>()
            val profile = async {
                manager.withCredentials(includeRefresh = true) { access, refresh ->
                    pairs += access to refresh
                    if (pairs.size == 1) {
                        firstLeaseObtained.complete(Unit)
                        allowFirstResponse.await()
                        throw unauthorized()
                    }
                    Unit
                }
            }
            try {
                firstLeaseObtained.await()
                var attempts = 0
                manager.authorized {
                    if (attempts++ == 0) throw unauthorized()
                }
            } finally {
                allowFirstResponse.complete(Unit)
            }
            profile.await()

            assertEquals(1, fixture.refreshCount())
            assertEquals(2, pairs.size)
            assertTrue(
                "The first lease must use the original credential pair",
                pairs[0] == (INITIAL_ACCESS to INITIAL_REFRESH)
            )
            assertTrue(
                "The retried lease must use the rotated credential pair",
                pairs[1] == (ROTATED_ACCESS to ROTATED_REFRESH)
            )
        }
    }

    @Test(timeout = 10_000)
    fun refreshCookieIsSentOnlyOnTheProfilePatchAmongAuthorizedRequests() = runBlocking {
        SessionFixture().use { fixture ->
            val session = SessionStore()
            val manager = fixture.manager(session, GatedRefreshTokenStore())
            manager.login("demo", "test-password")
            val client = AuthenticatedHttpClient(fixture.client, fixture.configuration, manager)
            val requestsBefore = fixture.requests().size

            client.request("auth/me")
            client.request("auth/me", HttpMethod.Patch)
            client.request("events")
            client.request("events/1", HttpMethod.Patch)
            client.request("users/1", HttpMethod.Patch)

            val requests = fixture.requests().drop(requestsBefore)
            assertEquals(5, requests.size)
            for (request in requests) {
                assertTrue(
                    "Every authorized call needs its access credential",
                    request.headers[HttpHeaders.Authorization] == "Bearer $INITIAL_ACCESS"
                )
                if (request.url.encodedPath == "/api/auth/me" && request.method == HttpMethod.Patch) {
                    assertTrue(
                        "Profile updates retain their refresh session family",
                        request.headers[HttpHeaders.Cookie] == AuthAPI.refreshCookie(INITIAL_REFRESH)
                    )
                } else {
                    assertNull(
                        "Refresh credentials must not leak to other endpoints",
                        request.headers[HttpHeaders.Cookie]
                    )
                }
            }
        }
    }

    @Test(timeout = 10_000)
    fun aSuccessfulResponseFromBeforeLogoutCannotEscapeIntoTheNextSessionGeneration() = runBlocking {
        SessionFixture().use { fixture ->
            val session = SessionStore()
            val manager = fixture.manager(session, GatedRefreshTokenStore())
            manager.login("demo", "test-password")
            val requestStarted = CompletableDeferred<Unit>()
            val allowResponse = CompletableDeferred<Unit>()
            val request = async {
                runCatching {
                    manager.authorized {
                        requestStarted.complete(Unit)
                        allowResponse.await()
                        "private-response"
                    }
                }.exceptionOrNull()
            }
            try {
                requestStarted.await()
                manager.logout()
            } finally {
                allowResponse.complete(Unit)
            }

            assertTrue(
                "A response from a revoked generation must be rejected",
                request.await() is AuthenticationException
            )
            assertNull(session.getUser())
            assertEquals(0, fixture.refreshCount())
        }
    }

    @Test(timeout = 10_000)
    fun logoutAllSerializesANewLoginAndCannotClearOrRevokeItsSession() = runBlocking {
        val session = SessionStore()
        val logoutAllStarted = CompletableDeferred<Unit>()
        val allowLogoutAllResponse = CompletableDeferred<Unit>()
        val secondLoginStarted = CompletableDeferred<Unit>()
        val loginCount = AtomicInteger()
        val nextAccess = "test-only-next-session-access"
        val nextRefresh = "test-only-next-session-refresh"
        SessionFixture { request ->
            when (request.url.encodedPath) {
                "/api/auth/logout-all" -> {
                    logoutAllStarted.complete(Unit)
                    allowLogoutAllResponse.await()
                    Reply()
                }

                "/api/auth/login" -> {
                    if (loginCount.incrementAndGet() == 2) {
                        secondLoginStarted.complete(Unit)
                        Reply.tokens(nextAccess, nextRefresh)
                    } else null
                }

                "/api/auth/me" -> {
                    if (request.headers[HttpHeaders.Authorization] == "Bearer $nextAccess") {
                        Reply(body = ApiTestFixture.userJson.replace("\"id\":1", "\"id\":2"))
                    } else null
                }

                else -> null
            }
        }.use { fixture ->
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val clearedUsers = mutableListOf<Int?>()
            store.beforeClear = { clearedUsers += session.getUser()?.id }
            val logoutAll = async { manager.logoutAll() }
            logoutAllStarted.await()
            val login = async(start = CoroutineStart.UNDISPATCHED) {
                manager.login("next-user", "test-password")
            }
            try {
                val reachedServerWhileLogoutPending = withTimeoutOrNull(250) {
                    secondLoginStarted.await()
                    true
                }
                assertNull("The next login must wait for the logout-all transaction", reachedServerWhileLogoutPending)
                assertFalse(login.isCompleted)
                assertEquals(1, session.getUser()?.id)
            } finally {
                allowLogoutAllResponse.complete(Unit)
            }
            logoutAll.await()
            assertEquals(2, login.await().id)

            assertEquals("Only the previous local session is cleared", listOf(1), clearedUsers)
            assertEquals(2, session.getUser()?.id)
            assertTrue("The next session keeps its access credential", session.getToken() == nextAccess)
            assertTrue("The next session remains restorable", storedRefresh(store.value) == nextRefresh)
            val revocations = fixture.requests().filter { it.url.encodedPath.endsWith("/logout-all") }
            assertEquals(1, revocations.size)
            assertTrue(
                "Only the previous session may authorize revocation",
                revocations.single().headers[HttpHeaders.Authorization] == "Bearer $INITIAL_ACCESS"
            )
            assertTrue(
                "The new session must not be revoked by a follow-up logout",
                fixture.requests().none { it.url.encodedPath == "/api/auth/logout" }
            )
            assertEquals(0, fixture.refreshCount())
        }
    }

    @Test(timeout = 10_000)
    fun forbiddenAndUnavailableLogoutAllResponsesPreserveLocalCredentials() = runBlocking {
        for (status in listOf(HttpStatusCode.Forbidden, HttpStatusCode.ServiceUnavailable)) {
            SessionFixture { request ->
                if (request.url.encodedPath == "/api/auth/logout-all") Reply(status = status) else null
            }.use { fixture ->
                val session = SessionStore()
                val store = GatedRefreshTokenStore()
                val manager = fixture.manager(session, store)
                manager.login("demo", "test-password")
                val persisted = store.value

                val failure = runCatching { manager.logoutAll() }.exceptionOrNull()

                assertEquals(status, (failure as ApiHttpException).status)
                assertEquals(1, session.getUser()?.id)
                assertTrue(
                    "Failed revocation must retain the current access credential",
                    session.getToken() == INITIAL_ACCESS
                )
                assertTrue("Failed revocation must retain the persisted refresh", store.value == persisted)
                assertEquals(0, fixture.refreshCount())
                assertEquals(1, fixture.requests().count { it.url.encodedPath == "/api/auth/logout-all" })
                assertTrue(fixture.requests().none { it.url.encodedPath == "/api/auth/logout" })
            }
        }
    }

    private suspend fun assertConcurrentRefreshIsCoalesced(sameAccessToken: Boolean) = coroutineScope {
        SessionFixture { request ->
            if (request.url.encodedPath.endsWith("/refresh") && sameAccessToken) {
                Reply.tokens(INITIAL_ACCESS, ROTATED_REFRESH)
            } else null
        }.use { fixture ->
            val session = SessionStore()
            val store = GatedRefreshTokenStore()
            val manager = fixture.manager(session, store)
            manager.login("demo", "test-password")
            val initialAttempts = AtomicInteger()
            val bothInitialAttemptsStarted = CompletableDeferred<Unit>()
            val calls = List(2) {
                async {
                    var attempts = 0
                    manager.authorized { access ->
                        attempts++
                        if (attempts == 1) {
                            if (initialAttempts.incrementAndGet() == 2) bothInitialAttemptsStarted.complete(Unit)
                            bothInitialAttemptsStarted.await()
                            throw unauthorized()
                        }
                        val expected = if (sameAccessToken) INITIAL_ACCESS else ROTATED_ACCESS
                        assertTrue("A retry must use the current access credential", access == expected)
                    }
                    attempts
                }
            }.awaitAll()

            assertEquals(listOf(2, 2), calls)
            assertEquals("Concurrent failures must rotate refresh exactly once", 1, fixture.refreshCount())
            assertTrue("The one rotated refresh must be durable", storedRefresh(store.value) == ROTATED_REFRESH)
        }
    }

    private class SessionFixture(
        private val overrideResponse: suspend (HttpRequestData) -> Reply? = { null },
    ) : AutoCloseable {
        val configuration = ApiConfiguration("https://calendar.example.test/api/")
        val json = Json { ignoreUnknownKeys = true }
        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        private val recordedRequests = Collections.synchronizedList(mutableListOf<HttpRequestData>())
        val client = HttpClient(
            MockEngine { request ->
                recordedRequests += request
                val reply = overrideResponse(request) ?: when (request.url.encodedPath) {
                    "/api/auth/login" -> Reply.tokens(INITIAL_ACCESS, INITIAL_REFRESH)
                    "/api/auth/refresh" -> Reply.tokens(ROTATED_ACCESS, ROTATED_REFRESH)
                    "/api/auth/me" -> Reply(body = ApiTestFixture.userJson)
                    else -> Reply()
                }
                respond(
                    reply.body, reply.status,
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        reply.refresh?.let {
                            append(
                                HttpHeaders.SetCookie,
                                "refresh_token=$it; Path=/api/auth; Max-Age=2592000; HttpOnly; Secure; SameSite=Strict"
                            )
                        }
                    }
                )
            }
        ) {
            expectSuccess = false
            followRedirects = false
            install(ContentNegotiation) { json(json) }
        }

        fun manager(session: SessionStore, store: RefreshTokenStore): RemoteSessionManager =
            RemoteSessionManager(
                AuthAPI(client, configuration),
                session,
                store,
                configuration,
                json,
                Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC),
                applicationScope,
            )

        fun requests(): List<HttpRequestData> = synchronized(recordedRequests) { recordedRequests.toList() }

        fun refreshRequests(): List<HttpRequestData> = requests().filter { it.url.encodedPath == "/api/auth/refresh" }

        fun refreshCount(): Int = refreshRequests().size

        override fun close() {
            applicationScope.cancel()
            client.close()
        }
    }

    private class GatedRefreshTokenStore(initial: String? = null) : RefreshTokenStore {
        @Volatile
        var value: String? = initial
            private set
        var beforeWrite: suspend () -> Unit = {}
        var beforeClear: suspend () -> Unit = {}

        override suspend fun read(): String? = value

        override suspend fun write(token: String) {
            beforeWrite()
            value = token
        }

        override suspend fun clear() {
            beforeClear()
            value = null
        }
    }

    private data class Reply(
        val body: String = "{}",
        val status: HttpStatusCode = HttpStatusCode.OK,
        val refresh: String? = null,
    ) {
        companion object {
            fun tokens(access: String, refresh: String): Reply = Reply(
                body = """{"access_token":"$access","token_type":"bearer","expires_in":900}""",
                refresh = refresh,
            )
        }
    }

    companion object {
        private const val INITIAL_ACCESS = "test-only-initial-access"
        private const val ROTATED_ACCESS = "test-only-rotated-access"
        private const val INITIAL_REFRESH = "test-only-initial-refresh"
        private const val ROTATED_REFRESH = "test-only-rotated-refresh"

        private fun unauthorized() = ApiHttpException(HttpStatusCode.Unauthorized, "Unauthorized")

        private fun storedRefresh(value: String?): String? =
            value?.let { Json.parseToJsonElement(it).jsonObject.getValue("token").jsonPrimitive.content }
    }
}
