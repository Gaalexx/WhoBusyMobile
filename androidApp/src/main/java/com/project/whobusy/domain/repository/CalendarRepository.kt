package com.project.whobusy.domain.repository

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventDraft

interface CalendarRepository {
    suspend fun getUsers(): Result<List<CalendarUser>>

    suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>>

    suspend fun getVisibleOwnerIds(): Result<Set<Int>>

    suspend fun getAccessibleOwnerIds(): Result<Set<Int>>

    suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit>

    suspend fun saveEvent(
        draft: EventDraft,
        eventId: Int? = null,
    ): Result<CalendarEvent>

    suspend fun deleteEvent(eventId: Int): Result<Unit>
}
