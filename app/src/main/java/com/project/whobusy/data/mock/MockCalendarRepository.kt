package com.project.whobusy.data.mock

import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.domain.repository.CalendarRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class MockCalendarRepository
    @Inject
    constructor(
        private val sessionStore: SessionStore,
        clock: Clock,
    ) : CalendarRepository {
        private val events = DemoCalendarData.events(LocalDate.now(clock)).toMutableList()
        private val accessibleOwnerIds = setOf(2, 3, 4)
        private var visibleOwnerIds = setOf(2, 3)

        override suspend fun getUsers(): Result<List<CalendarUser>> =
            repositoryResult {
                requireUser()
                DemoCalendarData.users
            }

        override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> =
            repositoryResult {
                val user = requireUser()
                val selected = query.ownerIds.ifEmpty { visibleOwnerIds }
                val allowed = selected.intersect(accessibleOwnerIds) + user.id
                events.filter { event ->
                    event.ownerId in allowed &&
                        (
                            event.recurrenceRule != null ||
                                (!event.end.isBefore(query.start) && !event.start.isAfter(query.end))
                        )
                }
            }

        override suspend fun getVisibleOwnerIds(): Result<Set<Int>> =
            repositoryResult {
                requireUser()
                visibleOwnerIds.toSet()
            }

        override suspend fun getAccessibleOwnerIds(): Result<Set<Int>> =
            repositoryResult {
                requireUser()
                accessibleOwnerIds
            }

        override suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit> =
            repositoryResult {
                requireUser()
                visibleOwnerIds = ownerIds.intersect(accessibleOwnerIds)
            }

        override suspend fun saveEvent(
            draft: EventDraft,
            eventId: Int?,
        ): Result<CalendarEvent> =
            repositoryResult {
                val user = requireUser()
                val original = eventId?.let { id -> events.first { it.id == id } }
                require(original == null || original.ownerId == user.id || user.role == UserRole.ADMIN)
                val event =
                    CalendarEvent(
                        id = original?.id ?: ((events.maxOfOrNull { it.id } ?: 0) + 1),
                        ownerId = original?.ownerId ?: user.id,
                        ownerName = original?.ownerName ?: user.fullName,
                        title = draft.title.trim(),
                        description = draft.description,
                        location = draft.location,
                        start = draft.start,
                        end = draft.end,
                        allDay = draft.allDay,
                        status = draft.status,
                        displayColor = original?.displayColor ?: user.color,
                        recurrenceRule = original?.recurrenceRule,
                    )
                events.removeAll { it.id == event.id }
                events.add(event)
                event
            }

        override suspend fun deleteEvent(eventId: Int): Result<Unit> =
            repositoryResult {
                val user = requireUser()
                val event = events.first { it.id == eventId }
                require(event.ownerId == user.id || user.role == UserRole.ADMIN)
                events.remove(event)
                Unit
            }

        private fun requireUser(): CalendarUser = sessionStore.getUser() ?: throw AuthenticationException()
    }
