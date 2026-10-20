package com.project.whobusy.presentation.spaces.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.presentation.calendar.model.expandRecurringEvents
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_admin_assigned
import com.project.whobusy.resources.spaces_error_action
import com.project.whobusy.resources.spaces_error_key
import com.project.whobusy.resources.spaces_error_load
import com.project.whobusy.resources.spaces_error_login
import com.project.whobusy.resources.spaces_error_name
import com.project.whobusy.resources.spaces_error_unavailable
import com.project.whobusy.resources.spaces_invitation_sent
import com.project.whobusy.resources.spaces_key_copied
import com.project.whobusy.resources.spaces_key_updated
import com.project.whobusy.resources.spaces_name_saved
import com.project.whobusy.time.Clock
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.now
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.value
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class SpacesViewModel(
    private val workspace: WorkspaceRepository,
    private val calendar: CalendarRepository,
    private val auth: AuthRepository,
    private val clock: Clock,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SpacesState(today = LocalDate.now(clock)))
    val state = mutableState.asStateFlow()
    private var initializedRoute: Pair<Int?, Int?>? = null
    private var loadJob: Job? = null

    fun onEvent(command: SpacesCommand) {
        if (command == SpacesCommand.ResultHandled) {
            mutableState.update { it.copy(result = null) }
            return
        }
        val current = mutableState.value
        if (current.result != null || current.isSaving ||
            (current.isLoading && command !is SpacesCommand.Load)
        ) return
        when (command) {
            is SpacesCommand.Load -> load(command.spaceId, command.memberId)
            is SpacesCommand.NameChanged ->
                mutableState.update {
                    it.copy(
                        name = command.value,
                        error = null,
                        message = null,
                    )
                }

            is SpacesCommand.CodeChanged -> mutableState.update { it.copy(code = command.value, error = null) }
            is SpacesCommand.LoginChanged ->
                mutableState.update {
                    it.copy(
                        login = command.value,
                        error = null,
                        message = null,
                    )
                }

            is SpacesCommand.RoleChanged -> mutableState.update { it.copy(selectedRole = command.role) }
            is SpacesCommand.SuccessorSelected -> mutableState.update { it.copy(successorId = command.userId) }
            SpacesCommand.ShowPlans -> mutableState.update { it.copy(showPlans = true) }
            SpacesCommand.CopyCode ->
                mutableState.value.space?.inviteCode?.let {
                    mutableState.update { it.copy(message = Res.string.spaces_key_copied) }
                }

            else -> mutate(command)
        }
    }

    private fun load(
        spaceId: Int?,
        memberId: Int?,
    ) {
        loadJob?.cancel()
        mutableState.update { it.copy(isLoading = true, error = null, spaceId = spaceId, memberId = memberId) }
        loadJob =
            viewModelScope.launch {
                try {
                    val route = spaceId to memberId
                    loadData(initializeForm = initializedRoute != route)
                    initializedRoute = route
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    mutableState.update { it.copy(error = Res.string.spaces_error_load) }
                } finally {
                    if (isActive) mutableState.update { it.copy(isLoading = false) }
                }
            }
    }

    private suspend fun loadData(initializeForm: Boolean = false) {
        val current = mutableState.value
        val spaces = workspace.getSpaces().getOrThrow()
        val invitations = workspace.getInvitations().getOrThrow()
        val userId = auth.currentUser.first()?.id
        val space = spaces.firstOrNull { it.id == current.spaceId }
        val members = space?.let { workspace.getMembers(it.id).getOrThrow() }.orEmpty()
        val pending =
            space
                ?.takeIf { it.myRole.canManage }
                ?.let {
                    workspace
                        .getSpaceInvitations(
                            it.id,
                        ).getOrThrow()
                }.orEmpty()
        val today = LocalDate.now(clock)
        val series =
            space
                ?.let {
                    calendar
                        .getEvents(
                            CalendarQuery(
                                today.atStartOfDay(),
                                today.plusDays(1).atStartOfDay(),
                                spaceIds = setOf(it.id),
                                showOwn = false,
                            ),
                        ).getOrThrow()
                }.orEmpty()
        val events =
            withContext(Dispatchers.Default) {
                expandRecurringEvents(
                    series,
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay(),
                    checkCancellation = { ensureActive() },
                )
            }
        val target = members.firstOrNull { it.userId == current.memberId }
        mutableState.update {
            it.copy(
                spaces = spaces,
                invitations = invitations,
                currentUserId = userId,
                members = members,
                pendingInvitations = pending,
                events = events,
                today = today,
                name = if (initializeForm) space?.name.orEmpty() else it.name,
                selectedRole = if (initializeForm) target?.role ?: SpaceRole.Actor else it.selectedRole,
                successorId =
                    it.successorId?.takeIf { id -> members.any { member -> member.userId == id } }
                        ?: members
                            .firstOrNull { member ->
                                member.userId != userId && member.role == SpaceRole.Moderator
                            }?.userId
                        ?: members.firstOrNull { member -> member.userId != userId }?.userId,
                error = if (current.spaceId != null && space == null) Res.string.spaces_error_unavailable else null,
            )
        }
    }

    private fun mutate(command: SpacesCommand) {
        val current = mutableState.value
        if ((command == SpacesCommand.Create || command == SpacesCommand.Rename) &&
            current.name.trim().length !in 1..100
        ) {
            mutableState.update { it.copy(error = Res.string.spaces_error_name) }
            return
        }
        if (command == SpacesCommand.Join && current.code.isBlank()) {
            mutableState.update { it.copy(error = Res.string.spaces_error_key) }
            return
        }
        if (command == SpacesCommand.Invite && current.login.isBlank()) {
            mutableState.update { it.copy(error = Res.string.spaces_error_login) }
            return
        }
        mutableState.update { it.copy(isSaving = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                execute(command, current)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableState.update { it.copy(error = Res.string.spaces_error_action) }
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    private suspend fun execute(
        command: SpacesCommand,
        current: SpacesState,
    ) {
        when (command) {
            SpacesCommand.Create -> {
                val space = workspace.createSpace(current.name).getOrThrow()
                mutableState.update { it.copy(result = SpacesEffect.OpenSpace(space.id)) }
            }

            SpacesCommand.Join -> {
                val space = workspace.joinSpace(current.code).getOrThrow()
                mutableState.update { it.copy(result = SpacesEffect.OpenSpace(space.id)) }
            }

            is SpacesCommand.AcceptInvitation -> {
                val space = workspace.acceptInvitation(command.invitationId).getOrThrow()
                mutableState.update { it.copy(result = SpacesEffect.OpenSpace(space.id)) }
            }

            is SpacesCommand.DeclineInvitation -> {
                workspace.declineInvitation(command.invitationId).getOrThrow()
                loadData()
            }

            else -> executeInSpace(command, current)
        }
    }

    private suspend fun executeInSpace(
        command: SpacesCommand,
        current: SpacesState,
    ) {
        val spaceId = requireNotNull(current.spaceId)
        when (command) {
            SpacesCommand.Rename -> {
                workspace.renameSpace(spaceId, current.name).getOrThrow()
                loadData()
                mutableState.update { it.copy(message = Res.string.spaces_name_saved) }
            }

            SpacesCommand.Delete -> {
                workspace.deleteSpace(spaceId).getOrThrow()
                mutableState.update { it.copy(result = SpacesEffect.Deleted) }
            }

            SpacesCommand.Leave -> {
                workspace.leaveSpace(spaceId).getOrThrow()
                mutableState.update { it.copy(result = SpacesEffect.Left) }
            }

            SpacesCommand.TransferAdmin -> {
                workspace
                    .updateMemberRole(
                        spaceId,
                        requireNotNull(current.successorId),
                        SpaceRole.Admin,
                    ).getOrThrow()
                loadData()
                mutableState.update { it.copy(message = Res.string.spaces_admin_assigned) }
            }

            SpacesCommand.SaveRole -> {
                workspace
                    .updateMemberRole(
                        spaceId,
                        requireNotNull(current.memberId),
                        current.selectedRole,
                    ).getOrThrow()
                mutableState.update { it.copy(result = SpacesEffect.MemberSaved) }
            }

            SpacesCommand.RemoveMember -> {
                workspace.removeMember(spaceId, requireNotNull(current.memberId)).getOrThrow()
                val result =
                    if (current.memberId == current.currentUserId) SpacesEffect.Left else SpacesEffect.MemberSaved
                mutableState.update { it.copy(result = result) }
            }

            SpacesCommand.Invite -> {
                workspace.invite(spaceId, current.login, current.selectedRole).getOrThrow()
                loadData()
                mutableState.update { it.copy(login = "", message = Res.string.spaces_invitation_sent) }
            }

            is SpacesCommand.CancelInvitation -> {
                workspace.cancelInvitation(spaceId, command.invitationId).getOrThrow()
                loadData()
            }

            SpacesCommand.RegenerateCode -> {
                workspace.regenerateInviteCode(spaceId).getOrThrow()
                loadData()
                mutableState.update { it.copy(message = Res.string.spaces_key_updated) }
            }

            else -> Unit
        }
    }
}
