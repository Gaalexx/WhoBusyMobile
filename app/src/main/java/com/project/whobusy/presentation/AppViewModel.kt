package com.project.whobusy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.whobusy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel
@Inject
constructor(
    auth: AuthRepository,
) : ViewModel() {
    val user = auth.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
