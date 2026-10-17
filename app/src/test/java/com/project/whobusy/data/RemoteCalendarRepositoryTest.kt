package com.project.whobusy.data

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.remote.api.CalendarAPI
import com.project.whobusy.data.repository.RemoteCalendarRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.EventVisibility
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class RemoteCalendarRepositoryTest {
    @Test
    fun sendsSpaceFiltersAndMapsVisibilityPermissionsAndRecurrence() = runBlocking {
        ApiTestFixture { "[$eventJson]" to HttpStatusCode.OK }.use { fixture ->
            val session = authenticatedSession()
            val query = CalendarQuery(start, end, spaceIds = setOf(3, 2), showOwn = false)

            val event = repository(fixture, session).getEvents(query).getOrThrow().single()
            val request = fixture.requests.single()

            assertEquals("/api/events", request.url.encodedPath)
            assertEquals("2,3", request.url.parameters["space_ids"])
            assertEquals("false", request.url.parameters["own"])
            assertNull(request.url.parameters["user_ids"])
            assertEquals(start.toString(), request.url.parameters["start"])
            assertEquals(end.toString(), request.url.parameters["end"])
            assertEquals("Bearer test-jwt", request.headers[HttpHeaders.Authorization])
            assertEquals(LocalDateTime.of(2026, 10, 15, 9, 0), event.start)
            assertEquals(EventStatus.TENTATIVE, event.status)
            assertEquals("FREQ=WEEKLY", event.recurrenceRule)
            assertEquals("#8f4f68", event.displayColor)
            assertEquals("#8f4f68", event.color)
            assertEquals(
                listOf(EventShare(2, EventVisibility.Actors), EventShare(3, EventVisibility.Busy)),
                event.shares
            )
            assertTrue(event.canEdit)
            assertFalse(event.busyOnly)
            assertNull(event.spaceId)
            assertNull(event.seriesStart)
        }
    }

    @Test
    fun omitsOptionalFiltersToUseSavedBoardAndFiltersLegacyOwnersLocally() = runBlocking {
        ApiTestFixture { "[$eventJson]" to HttpStatusCode.OK }.use { fixture ->
            val repository = repository(fixture)
            val query = CalendarQuery(start, end, ownerIds = setOf(99))

            assertTrue(repository.getEvents(query).getOrThrow().isEmpty())
            val request = fixture.requests.single()
            assertNull(request.url.parameters["space_ids"])
            assertNull(request.url.parameters["own"])
            assertNull(request.url.parameters["user_ids"])
        }
    }

    @Test
    fun explicitEmptySpaceSelectionDoesNotFallBackToSavedBoard() = runBlocking {
        ApiTestFixture { "[]" to HttpStatusCode.OK }.use { fixture ->
            repository(fixture).getEvents(CalendarQuery(start, end, spaceIds = emptySet(), showOwn = true)).getOrThrow()

            val request = fixture.requests.single()
            assertEquals("", request.url.parameters["space_ids"])
            assertEquals("true", request.url.parameters["own"])
        }
    }

    @Test
    fun mapsBusyOnlyResponseWithoutRestoringMaskedDetails() = runBlocking {
        val masked = """
            {"id":8,"owner_id":2,"owner_name":"Анна","title":"Занято",
             "description":"","location":"","start":"2026-10-15T09:00:00",
             "end":"2026-10-15T10:00:00","space_id":null,"shares":[],
             "busy_only":true,"can_edit":false,"display_color":"#4c5c9e","color":null}
        """.trimIndent()
        ApiTestFixture { "[$masked]" to HttpStatusCode.OK }.use { fixture ->
            val event = repository(fixture).getEvents(CalendarQuery(start, end)).getOrThrow().single()

            assertTrue(event.busyOnly)
            assertFalse(event.canEdit)
            assertEquals("Занято", event.title)
            assertEquals("", event.description)
            assertEquals("", event.location)
            assertTrue(event.shares.isEmpty())
            assertNull(event.color)
        }
    }

    @Test
    fun createsPersonalPlanWithSharesColorAndRecurrence() = runBlocking {
        ApiTestFixture { eventJson to HttpStatusCode.Created }.use { fixture ->
            val draft = draft().copy(
                title = "  Встреча  ",
                shares = listOf(EventShare(2, EventVisibility.Actors), EventShare(3, EventVisibility.Busy)),
                recurrenceRule = "FREQ=WEEKLY",
                color = "#8f4f68",
            )

            repository(fixture).saveEvent(draft).getOrThrow()
            val request = fixture.requests.single()
            val payload = request.payload()

            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/events", request.url.encodedPath)
            assertEquals("Встреча", payload.getValue("title").jsonPrimitive.content)
            assertEquals(JsonNull, payload["space_id"])
            assertEquals(
                "actors",
                payload.getValue("shares").jsonArray[0].jsonObject["visibility"]?.jsonPrimitive?.content
            )
            assertEquals(
                "busy",
                payload.getValue("shares").jsonArray[1].jsonObject["visibility"]?.jsonPrimitive?.content
            )
            assertEquals("FREQ=WEEKLY", payload.getValue("rrule").jsonPrimitive.content)
            assertEquals("#8f4f68", payload.getValue("color").jsonPrimitive.content)
            assertFalse("can_edit" in payload)
            assertFalse("busy_only" in payload)
        }
    }

    @Test
    fun createsSpacePlanWithPlacementAndWithoutShares() = runBlocking {
        ApiTestFixture { eventJson to HttpStatusCode.Created }.use { fixture ->
            repository(fixture).saveEvent(draft().copy(spaceId = 3)).getOrThrow()
            val payload = fixture.requests.single().payload()

            assertEquals("3", payload.getValue("space_id").jsonPrimitive.content)
            assertTrue(payload.getValue("shares").jsonArray.isEmpty())
        }
    }

    @Test
    fun updatingPersonalPlanExplicitlyClearsRecurrenceColorAndShares() = runBlocking {
        ApiTestFixture { eventJson to HttpStatusCode.OK }.use { fixture ->
            repository(fixture).saveEvent(draft(), eventId = 7).getOrThrow()
            val request = fixture.requests.single()
            val payload = request.payload()

            assertEquals(HttpMethod.Patch, request.method)
            assertEquals("/api/events/7", request.url.encodedPath)
            assertFalse("space_id" in payload)
            assertEquals(JsonNull, payload["rrule"])
            assertEquals(JsonNull, payload["color"])
            assertTrue(payload.getValue("shares").jsonArray.isEmpty())
        }
    }

    @Test
    fun spacePlanPatchOmitsPlacementAndOwnerOnlyShares() = runBlocking {
        val response = eventJson.replace("\"space_id\":null", "\"space_id\":3")
        ApiTestFixture { response to HttpStatusCode.OK }.use { fixture ->
            val event = repository(fixture).saveEvent(draft().copy(spaceId = 3), eventId = 7).getOrThrow()
            val payload = fixture.requests.single().payload()

            assertEquals(3, event.spaceId)
            assertFalse("space_id" in payload)
            assertFalse("shares" in payload)
            assertTrue("title" in payload)
        }
    }

    @Test
    fun deletesPlanWithoutParsingNoContentResponse() = runBlocking {
        ApiTestFixture { "" to HttpStatusCode.NoContent }.use { fixture ->
            repository(fixture).deleteEvent(7).getOrThrow()

            assertEquals(HttpMethod.Delete, fixture.requests.single().method)
            assertEquals("/api/events/7", fixture.requests.single().url.encodedPath)
        }
    }

    @Test
    fun permissionFailureDoesNotClearSessionOrRetryMutation() = runBlocking {
        ApiTestFixture { """{"detail":"You can't edit this plan"}""" to HttpStatusCode.Forbidden }.use { fixture ->
            val session = authenticatedSession()

            assertTrue(repository(fixture, session).deleteEvent(7).isFailure)
            assertEquals("test-jwt", session.getToken())
            assertEquals(DemoAccount.user, session.getUser())
            assertEquals(1, fixture.requests.size)
        }
    }

    @Test
    fun legacyOwnerSelectionFailsExplicitlyWithoutCallingRemovedEndpoints() = runBlocking {
        ApiTestFixture { error("Legacy owner methods must not make a request") }.use { fixture ->
            val repository = repository(fixture)

            assertTrue(repository.getUsers().exceptionOrNull() is UnsupportedOperationException)
            assertTrue(repository.getVisibleOwnerIds().exceptionOrNull() is UnsupportedOperationException)
            assertTrue(repository.getAccessibleOwnerIds().exceptionOrNull() is UnsupportedOperationException)
            assertTrue(repository.setVisibleOwnerIds(setOf(2)).exceptionOrNull() is UnsupportedOperationException)
            assertTrue(fixture.requests.isEmpty())
        }
    }

    private fun repository(fixture: ApiTestFixture, session: SessionStore = authenticatedSession()) =
        RemoteCalendarRepository(CalendarAPI(fixture.authenticatedClient(session)))

    private fun authenticatedSession() = SessionStore().apply { save("test-jwt", DemoAccount.user) }

    private fun draft() = EventDraft("Встреча", start, start.plusHours(1))

    private fun HttpRequestData.payload() = Json.parseToJsonElement((body as TextContent).text).jsonObject

    companion object {
        private val start = LocalDateTime.parse("2026-10-01T00:00")
        private val end = LocalDateTime.parse("2026-11-01T00:00")
        private val eventJson = """
            {"id":7,"owner_id":2,"owner_name":"Анна","title":"Встреча",
             "start":"2026-10-15T09:00:00","end":"2026-10-15T10:00:00",
             "status":"tentative","display_color":"#8f4f68","rrule":"FREQ=WEEKLY",
             "space_id":null,"shares":[{"space_id":2,"visibility":"actors"},{"space_id":3,"visibility":"busy"}],
             "busy_only":false,"can_edit":true,"color":"#8f4f68"}
        """.trimIndent()
    }
}
