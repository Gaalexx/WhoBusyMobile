package com.project.whobusy.data.mock

import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.domain.repository.CalendarRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockCalendarRepository
@Inject
constructor(
    private val sessionStore: SessionStore,
    clock: Clock,
    private val workspaces: MockWorkspaceRepository,
    private val accounts: MockAccountStore,
) : CalendarRepository {
    constructor(sessionStore: SessionStore, clock: Clock) : this(
        sessionStore,
        clock,
        MockWorkspaceRepository(sessionStore, MockAccountStore(), clock),
        MockAccountStore(),
    )

    private val lock = Any()
    private val events = MockPlanData.events(LocalDate.now(clock)).toMutableList()
    private var detachmentSequence = 0
    private var nextId = (events.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun getUsers(): Result<List<CalendarUser>> =
        calendarResult {
            val user = requireUser()
            val board = workspaces.boardFor(user.id)
            val visible =
                events
                    .mapNotNull { visibleEvent(it, user, board.spaceIds, board.showOwn) }
                    .map { it.ownerId }
                    .toSet() + user.id
            accounts.users.value.filter { it.id in visible }.map {
                if (it.id == user.id) it else it.copy(email = null)
            }
        }

    override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> =
        calendarResult {
            val user = requireUser()
            reconcileWorkspaceChanges()
            val board = workspaces.boardFor(user.id)
            val selected =
                (query.spaceIds ?: board.spaceIds)
                    .filter {
                        workspaces.roleFor(
                            it,
                            user.id,
                        ) != null
                    }.toSet()
            val own = query.showOwn ?: board.showOwn
            events
                .mapNotNull { event ->
                    if (event.recurrenceRule == null && (event.end < query.start || event.start > query.end)) {
                        return@mapNotNull null
                    }
                    visibleEvent(event, user, selected, own)
                }.sortedBy { it.start }
        }

    // Kept for source compatibility with the previous colleague-picker route.
    // The board now filters spaces; these methods never grant access.
    override suspend fun getVisibleOwnerIds(): Result<Set<Int>> = getAccessibleOwnerIds()

    override suspend fun getAccessibleOwnerIds(): Result<Set<Int>> =
        getEvents(
            CalendarQuery(LocalDate.of(2000, 1, 1).atStartOfDay(), LocalDate.of(2100, 1, 1).atStartOfDay()),
        ).map { it.map(CalendarEvent::ownerId).toSet() - requireUser().id }

    override suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit> =
        calendarResult {
            requireUser()
            Unit
        }

    override suspend fun saveEvent(
        draft: EventDraft,
        eventId: Int?,
    ): Result<CalendarEvent> =
        calendarResult {
            val user = requireUser()
            reconcileWorkspaceChanges()
            val original = eventId?.let { id -> events.first { it.id == id } }
            require(original == null || canEdit(original, user.id))
            require(original == null || original.spaceId == draft.spaceId) { "Event placement cannot change" }
            draft.spaceId?.let { require(workspaces.roleFor(it, user.id) != null) }
            require(draft.spaceId == null || draft.shares.isEmpty())
            require(
                draft.shares
                    .map(EventShare::spaceId)
                    .distinct()
                    .size == draft.shares.size,
            )
            draft.shares.forEach { require(workspaces.roleFor(it.spaceId, user.id) != null) }
            require(original == null || original.ownerId == user.id || original.shares == draft.shares)
            require(draft.title.trim().length in 1..200)
            require(draft.color == null || Regex("#[0-9a-fA-F]{6}").matches(draft.color))
            val event =
                CalendarEvent(
                    id = original?.id ?: nextId++,
                    ownerId = original?.ownerId ?: user.id,
                    ownerName = original?.ownerName ?: user.fullName,
                    title = draft.title.trim(),
                    description = draft.description,
                    location = draft.location,
                    start = draft.start,
                    end = draft.end,
                    allDay = draft.allDay,
                    status = draft.status,
                    displayColor =
                        draft.color ?: accounts.users.value
                            .first { it.id == (original?.ownerId ?: user.id) }
                            .color,
                    recurrenceRule = draft.recurrenceRule,
                    spaceId = draft.spaceId,
                    shares = draft.shares,
                    color = draft.color,
                )
            events.removeAll { it.id == event.id }
            events.add(event)
            event.copy(canEdit = true)
        }

    override suspend fun deleteEvent(eventId: Int): Result<Unit> =
        calendarResult {
            val user = requireUser()
            reconcileWorkspaceChanges()
            val event = events.first { it.id == eventId }
            require(canEdit(event, user.id))
            events.remove(event)
            Unit
        }

    private fun visibleEvent(
        event: CalendarEvent,
        user: CalendarUser,
        spaces: Set<Int>,
        own: Boolean,
    ): CalendarEvent? {
        val spaceVisible = event.spaceId in spaces
        val shares =
            event.shares.filter {
                it.spaceId in spaces &&
                    (
                        event.ownerId == user.id || it.visibility == EventVisibility.Busy ||
                            workspaces.roleFor(it.spaceId, user.id) != SpaceRole.Visitor
                        )
            }
        if (!(own && event.ownerId == user.id) && !spaceVisible && shares.isEmpty()) return null
        val full =
            event.ownerId == user.id || spaceVisible ||
                shares.any {
                    it.visibility == EventVisibility.Actors &&
                        workspaces.roleFor(it.spaceId, user.id) != SpaceRole.Visitor
                }
        val owner = accounts.users.value.firstOrNull { it.id == event.ownerId } ?: return null
        return event.copy(
            ownerName = owner.fullName,
            title = if (full) event.title else "Занято",
            description = if (full) event.description else "",
            location = if (full) event.location else "",
            color = if (full) event.color else null,
            displayColor = if (full) event.color ?: owner.color else owner.color,
            shares = if (event.ownerId == user.id) event.shares else emptyList(),
            busyOnly = !full,
            canEdit = canEdit(event, user.id),
        )
    }

    private fun canEdit(
        event: CalendarEvent,
        userId: Int,
    ): Boolean = event.ownerId == userId || event.spaceId?.let { workspaces.roleFor(it, userId)?.canManage } == true

    private fun reconcileWorkspaceChanges() {
        events.removeAll { it.ownerId in workspaces.deletedUserIds }
        workspaces.detachmentsAfter(detachmentSequence).forEach { change ->
            events.replaceAll { event ->
                val affectedOwner = change.userId == null || change.userId == event.ownerId
                event.copy(
                    spaceId = event.spaceId.takeUnless { it == change.spaceId && affectedOwner },
                    shares =
                        if (affectedOwner) {
                            event.shares.filterNot {
                                it.spaceId == change.spaceId
                            }
                        } else {
                            event.shares
                        },
                )
            }
            detachmentSequence = change.sequence
        }
    }

    private fun requireUser(): CalendarUser {
        val id = sessionStore.getUser()?.id ?: throw AuthenticationException()
        return accounts.users.value.firstOrNull { it.id == id && it.isActive } ?: throw AuthenticationException()
    }

    private suspend fun <Value> calendarResult(block: () -> Value): Result<Value> =
        repositoryResult { synchronized(lock, block) }
}
