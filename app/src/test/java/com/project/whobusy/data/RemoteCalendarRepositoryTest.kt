package com.project.whobusy.data

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.remote.api.CalendarAPI
import com.project.whobusy.data.repository.RemoteCalendarRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventStatus
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class RemoteCalendarRepositoryTest {
    @Test
    fun sendsRangeAndOwnerIdsAndMapsFloatingDatesAndRecurrence() =
        runBlocking {
            val response =
                """
                [{"id":7,"owner_id":2,"owner_name":"Анна","title":"Встреча",
                  "start":"2026-10-15T09:00:00","end":"2026-10-15T10:00:00",
                  "status":"tentative","display_color":"#8f4f68","rrule":"FREQ=WEEKLY"}]
                """.trimIndent()
            ApiTestFixture { response to HttpStatusCode.OK }.use { fixture ->
                val session = authenticatedSession()
                val repository =
                    RemoteCalendarRepository(
                        CalendarAPI(fixture.client, fixture.configuration, session),
                        session,
                    )
                val query =
                    CalendarQuery(
                        LocalDateTime.parse("2026-10-01T00:00"),
                        LocalDateTime.parse("2026-11-01T00:00"),
                        setOf(3, 2),
                    )

                val event = repository.getEvents(query).getOrThrow().single()
                val request = fixture.requests.single()

                assertEquals("/api/events", request.url.encodedPath)
                assertEquals("2,3", request.url.parameters["user_ids"])
                assertEquals(query.start.toString(), request.url.parameters["start"])
                assertEquals(query.end.toString(), request.url.parameters["end"])
                assertEquals("Bearer test-jwt", request.headers[HttpHeaders.Authorization])
                assertEquals(LocalDateTime.of(2026, 10, 15, 9, 0), event.start)
                assertEquals(EventStatus.TENTATIVE, event.status)
                assertEquals("FREQ=WEEKLY", event.recurrenceRule)
                assertEquals("#8f4f68", event.displayColor)
            }
        }

    @Test
    fun unauthorizedResponseClearsSession() =
        runBlocking {
            ApiTestFixture { "{}" to HttpStatusCode.Unauthorized }.use { fixture ->
                val session = authenticatedSession()
                val repository =
                    RemoteCalendarRepository(
                        CalendarAPI(fixture.client, fixture.configuration, session),
                        session,
                    )

                assertTrue(repository.getUsers().exceptionOrNull() is AuthenticationException)
                assertNull(session.getToken())
                assertNull(session.getUser())
            }
        }

    @Test
    fun writesVisibilityUsingBackendFieldName() =
        runBlocking {
            ApiTestFixture { "[2,3]" to HttpStatusCode.OK }.use { fixture ->
                val session = authenticatedSession()
                val repository =
                    RemoteCalendarRepository(
                        CalendarAPI(fixture.client, fixture.configuration, session),
                        session,
                    )

                repository.setVisibleOwnerIds(setOf(3, 2)).getOrThrow()
                val request = fixture.requests.single()
                val payload = Json.parseToJsonElement((request.body as TextContent).text).jsonObject

                assertEquals(HttpMethod.Put, request.method)
                assertEquals("/api/users/me/visibility", request.url.encodedPath)
                assertEquals("[2,3]", payload.getValue("visible_user_ids").jsonArray.toString())
            }
        }

    private fun authenticatedSession() =
        SessionStore().apply {
            save("test-jwt", DemoAccount.user)
        }
}
