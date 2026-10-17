package com.project.whobusy.data.remote.api

import com.project.whobusy.data.network.AuthenticatedHttpClient
import com.project.whobusy.data.remote.dto.CalendarEventDTO
import com.project.whobusy.data.remote.dto.EventRequestDTO
import com.project.whobusy.data.remote.dto.EventUpdateRequestDTO
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventDraft
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import javax.inject.Inject

class CalendarAPI
@Inject
constructor(
    private val client: AuthenticatedHttpClient,
) {
    suspend fun getEvents(query: CalendarQuery): List<CalendarEventDTO> =
        client
            .request("events") {
                parameter("start", query.start.toString())
                parameter("end", query.end.toString())
                query.spaceIds?.let {
                    parameter("space_ids", it.sorted().joinToString(","))
                }
                query.showOwn?.let { parameter("own", it) }
            }.body()

    suspend fun saveEvent(
        draft: EventDraft,
        eventId: Int?,
    ): CalendarEventDTO =
        client
            .request(
                path = if (eventId == null) "events" else "events/$eventId",
                method = if (eventId == null) HttpMethod.Post else HttpMethod.Patch,
            ) {
                contentType(ContentType.Application.Json)
                if (eventId == null) {
                    setBody(EventRequestDTO.fromDomain(draft))
                } else {
                    setBody(EventUpdateRequestDTO.fromDomain(draft))
                }
            }.body()

    suspend fun deleteEvent(eventId: Int) {
        client.request("events/$eventId", HttpMethod.Delete)
    }
}
