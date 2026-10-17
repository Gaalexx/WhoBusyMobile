package com.project.whobusy.data.repository

import com.project.whobusy.data.remote.api.CalendarAPI
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.repository.CalendarRepository
import javax.inject.Inject

class RemoteCalendarRepository
@Inject
constructor(
    private val api: CalendarAPI,
) : CalendarRepository {
    override suspend fun getUsers(): Result<List<CalendarUser>> =
        unsupportedOwnerSelection()

    override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> =
        repositoryResult {
            api.getEvents(query).map { it.toDomain() }.filter {
                query.ownerIds.isEmpty() || it.ownerId in query.ownerIds
            }
        }

    override suspend fun getVisibleOwnerIds(): Result<Set<Int>> =
        unsupportedOwnerSelection()

    override suspend fun getAccessibleOwnerIds(): Result<Set<Int>> =
        unsupportedOwnerSelection()

    override suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit> =
        unsupportedOwnerSelection()

    override suspend fun saveEvent(
        draft: EventDraft,
        eventId: Int?,
    ): Result<CalendarEvent> = repositoryResult { api.saveEvent(draft, eventId).toDomain() }

    override suspend fun deleteEvent(eventId: Int): Result<Unit> = repositoryResult { api.deleteEvent(eventId) }

    private fun <Value> unsupportedOwnerSelection(): Result<Value> =
        Result.failure(UnsupportedOperationException("Owner selection was replaced by workspace board settings"))
}
