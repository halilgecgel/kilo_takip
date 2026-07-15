package com.kilotakip.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilotakip.app.data.datastore.UserProfile
import com.kilotakip.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionState(
    val isChecking: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isAdmin: Boolean = false
)

@HiltViewModel
class MainNavViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = combine(
        authRepository.tokenFlow,
        authRepository.userRoleFlow
    ) { token, role ->
        SessionState(
            isChecking = false,
            isAuthenticated = !token.isNullOrBlank(),
            isAdmin = role == "admin"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionState())

    val userProfile: StateFlow<UserProfile> = authRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
