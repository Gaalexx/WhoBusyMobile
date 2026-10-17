package com.project.whobusy.data

import com.project.whobusy.data.repository.RemoteAuthRepository
import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.model.RegistrationDraft
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAuthRepositoryTest {
    @Test
    fun loginUsesLoginFieldAndPersistsOnlyRefreshAfterLoadingProfile() = runBlocking {
        ApiTestFixture { request ->
            (if (request.url.encodedPath.endsWith("login")) ApiTestFixture.tokenJson else ApiTestFixture.userJson) to HttpStatusCode.OK
        }.use { fixture ->
            val session = SessionStore()
            val store = MemoryRefreshTokenStore()
            val repository = RemoteAuthRepository(fixture.sessionManager(session, store), session)
            val user = repository.login(" DEMO@WHOBUSY.TEST ", "password").getOrThrow()
            val login = fixture.requests.first()
            val payload = Json.parseToJsonElement((login.body as TextContent).text).jsonObject

            assertEquals(HttpMethod.Post, login.method)
            assertEquals("/api/auth/login", login.url.encodedPath)
            assertEquals("demo@whobusy.test", payload.getValue("login").jsonPrimitive.content)
            assertFalse("email" in payload)
            assertEquals("password", payload.getValue("password").jsonPrimitive.content)
            assertNull(login.headers[HttpHeaders.Authorization])
            assertEquals("/api/auth/me", fixture.requests.last().url.encodedPath)
            assertEquals("Bearer test-jwt", fixture.requests.last().headers[HttpHeaders.Authorization])
            assertEquals(user, session.getUser())
            assertEquals("demo", user.login)
            assertEquals("test-jwt", session.getToken())
            assertTrue(store.value!!.contains("test-refresh"))
            assertFalse(store.value!!.contains("test-jwt"))
            assertFalse(store.value!!.contains("password"))
        }
    }

    @Test
    fun registrationMapsAllNamesAndOptionalEmail() = runBlocking {
        ApiTestFixture { request ->
            (if (request.url.encodedPath.endsWith("register")) ApiTestFixture.tokenJson else ApiTestFixture.userJson) to HttpStatusCode.OK
        }.use { fixture ->
            val session = SessionStore()
            val repo = RemoteAuthRepository(fixture.sessionManager(session), session)
            repo.register(RegistrationDraft(" test ", " Имя ", " Фамилия ", "secret", null)).getOrThrow()
            val request = fixture.requests.first()
            val payload = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
            assertEquals("/api/auth/register", request.url.encodedPath)
            assertEquals("test", payload.getValue("login").jsonPrimitive.content)
            assertEquals("Имя", payload.getValue("first_name").jsonPrimitive.content)
            assertEquals("Фамилия", payload.getValue("last_name").jsonPrimitive.content)
            assertNotNull(session.getUser())
        }
    }

    @Test
    fun rejectedCredentialsBecomeDomainError() = runBlocking {
        ApiTestFixture { """{"detail":"Invalid login or password"}""" to HttpStatusCode.Unauthorized }.use { fixture ->
            val session = SessionStore()
            val repo = RemoteAuthRepository(fixture.sessionManager(session), session)
            assertTrue(repo.login("demo", "incorrect").exceptionOrNull() is InvalidCredentialsException)
            assertEquals(1, fixture.requests.size)
            assertNull(session.getToken())
        }
    }

    @Test
    fun missingCookieAndFailedProfileNeverPublishPartialSession() = runBlocking {
        for (missingCookie in listOf(true, false)) {
            ApiTestFixture { request ->
                if (request.url.encodedPath.endsWith("login")) ApiTestFixture.tokenJson to HttpStatusCode.OK
                else "{}" to HttpStatusCode.ServiceUnavailable
            }.use { fixture ->
                if (missingCookie) fixture.headersFor =
                    { _, _ -> headersOf(HttpHeaders.ContentType, "application/json") }
                val session = SessionStore()
                val store = MemoryRefreshTokenStore()
                val repo = RemoteAuthRepository(fixture.sessionManager(session, store), session)
                assertTrue(repo.login("demo", "password").isFailure)
                assertNull(session.getToken())
                assertNull(session.getUser())
                assertNull(store.value)
            }
        }
    }

    @Test(expected = CancellationException::class)
    fun repositoryResultPreservesCoroutineCancellation() = runBlocking<Unit> {
        repositoryResult<Unit> { throw CancellationException("Cancelled") }
    }
}
