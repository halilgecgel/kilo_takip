package com.kilotakip.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilotakip.app.data.remote.dto.AdminUserDto
import com.kilotakip.app.data.remote.dto.BlacklistedIpDto
import com.kilotakip.app.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val users: List<AdminUserDto> = emptyList(),
    val blacklistedIps: List<BlacklistedIpDto> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val users = repository.getUsers()
            val ips = repository.getBlacklistedIps()
            _uiState.value = AdminUiState(users = users, blacklistedIps = ips, isLoading = false)
        }
    }

    fun toggleBan(user: AdminUserDto) {
        viewModelScope.launch {
            repository.banUser(user.id, user.status != "banned")
            refresh()
        }
    }

    fun endSessions(user: AdminUserDto) {
        viewModelScope.launch { repository.endSessions(user.id) }
    }

    fun blockIp(ip: String, reason: String?) {
        viewModelScope.launch {
            repository.addBlacklistedIp(ip, reason)
            refresh()
        }
    }

    fun unblockIp(id: Long) {
        viewModelScope.launch {
            repository.removeBlacklistedIp(id)
            refresh()
        }
    }
}
