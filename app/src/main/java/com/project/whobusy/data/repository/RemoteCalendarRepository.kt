package com.project.whobusy.data.repository

import com.project.whobusy.data.remote.api.CalendarAPI
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.repository.CalendarRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class RemoteCalendarRepository
@Inject
constructor(
    private val api: CalendarAPI,
    private val sessionStore: SessionStore,
) : CalendarRepository {
    override suspend fun getUsers(): Result<List<CalendarUser>> =
        authenticatedResult { api.getUsers().map { it.toDomain() } }

    override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> =
        authenticatedResult { api.getEvents(query).map { it.toDomain() } }

    override suspend fun getVisibleOwnerIds(): Result<Set<Int>> =
        authenticatedResult { api.getVisibleOwnerIds().toSet() }

    override suspend fun getAccessibleOwnerIds(): Result<Set<Int>> =
        authenticatedResult { api.getAccessibleOwnerIds().toSet() }

    override suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit> =
        authenticatedResult { api.setVisibleOwnerIds(ownerIds) }

    override suspend fun saveEvent(
        draft: EventDraft,
        eventId: Int?,
    ): Result<CalendarEvent> = authenticatedResult { api.saveEvent(draft, eventId).toDomain() }

    override suspend fun deleteEvent(eventId: Int): Result<Unit> = authenticatedResult { api.deleteEvent(eventId) }

    private suspend fun <Value> authenticatedResult(block: suspend () -> Value): Result<Value> =
        repositoryResult {
            try {
                block()
            } catch (exception: ClientRequestException) {
                if (exception.response.status == HttpStatusCode.Unauthorized) {
                    sessionStore.clear()
                    throw AuthenticationException()
                }
                throw exception
            }
        }
}
