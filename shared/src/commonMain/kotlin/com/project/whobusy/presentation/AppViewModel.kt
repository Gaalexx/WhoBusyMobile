package com.project.whobusy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.repository.AuthRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class AppViewModel(
    private val auth: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AppState())
    val state = mutableState.asStateFlow()
    private var startupJob: Job? = null

    init {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            auth.currentUser.collect { user -> mutableState.update { it.copy(user = user) } }
        }
        runStartup(auth::restoreSession)
    }

    fun onEvent(command: AppCommand) {
        if (mutableState.value.startup != AppStartup.Failed || startupJob?.isActive == true) return
        when (command) {
            AppCommand.RetryRestore -> runStartup(auth::restoreSession)
            AppCommand.SignInAgain -> runStartup(auth::logout)
        }
    }

    private fun runStartup(operation: suspend () -> Result<Unit>) {
        mutableState.update { it.copy(startup = AppStartup.Loading) }
        startupJob = viewModelScope.launch {
            try {
                operation().getOrThrow()
                // Read the published user before exposing navigation. A separate
                // stateIn collector may still be waiting for its Main dispatcher turn.
                val user = auth.currentUser.first()
                mutableState.update { it.copy(user = user, startup = AppStartup.Ready) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.update { it.copy(startup = AppStartup.Failed) }
            }
        }
    }
}
