package com.kilotakip.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilotakip.app.data.remote.NetworkResult
import com.kilotakip.app.data.remote.dto.HealthSummaryDto
import com.kilotakip.app.data.repository.AuthRepository
import com.kilotakip.app.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val healthSummary: HealthSummaryDto? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * @param login Kullanıcı adı veya telefon numarası.
     */
    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Kullanıcı adı/telefon ve şifre gerekli.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.login(login.trim(), password)) {
                is NetworkResult.Success -> {
                    syncScheduler.schedulePeriodicSync()
                    _uiState.value = AuthUiState(isAuthenticated = true, healthSummary = result.data.health)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun register(
        name: String,
        username: String,
        phone: String,
        password: String,
        birthDate: String,
        heightCm: Double?,
        currentWeightKg: Double?
    ) {
        if (name.isBlank() || username.isBlank() || phone.isBlank() || password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Lütfen tüm alanları doğru doldurun (şifre en az 6 karakter)."
            )
            return
        }
        if (birthDate.isBlank() || heightCm == null || currentWeightKg == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Doğum tarihi, boy ve kilo bilgisi gerekli."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = authRepository.register(
                    name.trim(), username.trim(), phone.trim(), password, birthDate, heightCm, currentWeightKg
                )
            ) {
                is NetworkResult.Success -> {
                    syncScheduler.schedulePeriodicSync()
                    _uiState.value = AuthUiState(isAuthenticated = true, healthSummary = result.data.health)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
