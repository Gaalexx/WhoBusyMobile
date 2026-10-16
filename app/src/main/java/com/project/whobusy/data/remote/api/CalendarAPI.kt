package com.project.whobusy.data.remote.api

import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.dto.CalendarEventDTO
import com.project.whobusy.data.remote.dto.EventRequestDTO
import com.project.whobusy.data.remote.dto.UserDTO
import com.project.whobusy.data.remote.dto.VisibilityRequestDTO
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventDraft
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import javax.inject.Inject

class CalendarAPI
    @Inject
    constructor(
        private val client: HttpClient,
        private val configuration: ApiConfiguration,
        private val sessionStore: SessionStore,
    ) {
        suspend fun getUsers(): List<UserDTO> =
            client
                .get(configuration.endpoint("users")) {
                    bearerAuth(requireToken())
                }.body()

        suspend fun getEvents(query: CalendarQuery): List<CalendarEventDTO> =
            client
                .get(configuration.endpoint("events")) {
                    bearerAuth(requireToken())
                    parameter("start", query.start.toString())
                    parameter("end", query.end.toString())
                    if (query.ownerIds.isNotEmpty()) {
                        parameter("user_ids", query.ownerIds.sorted().joinToString(","))
                    }
                }.body()

        suspend fun getVisibleOwnerIds(): List<Int> =
            client
                .get(configuration.endpoint("users/me/visibility")) {
                    bearerAuth(requireToken())
                }.body()

        suspend fun getAccessibleOwnerIds(): List<Int> =
            client
                .get(configuration.endpoint("users/me/visible-to-me")) {
                    bearerAuth(requireToken())
                }.body()

        suspend fun setVisibleOwnerIds(ownerIds: Set<Int>) {
            client.put(configuration.endpoint("users/me/visibility")) {
                bearerAuth(requireToken())
                contentType(ContentType.Application.Json)
                setBody(VisibilityRequestDTO(ownerIds.sorted()))
            }
        }

        suspend fun saveEvent(
            draft: EventDraft,
            eventId: Int?,
        ): CalendarEventDTO =
            client
                .request(configuration.endpoint(if (eventId == null) "events" else "events/$eventId")) {
                    method = if (eventId == null) HttpMethod.Post else HttpMethod.Patch
                    bearerAuth(requireToken())
                    contentType(ContentType.Application.Json)
                    setBody(EventRequestDTO.fromDomain(draft))
                }.body()

        suspend fun deleteEvent(eventId: Int) {
            client.request(configuration.endpoint("events/$eventId")) {
                method = HttpMethod.Delete
                bearerAuth(requireToken())
            }
        }

        private fun requireToken(): String = sessionStore.getToken() ?: throw AuthenticationException()
    }
