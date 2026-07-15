package com.kilotakip.app.data.repository

import com.kilotakip.app.data.datastore.SessionManager
import com.kilotakip.app.data.datastore.UserProfile
import com.kilotakip.app.data.remote.ApiService
import com.kilotakip.app.data.remote.NetworkResult
import com.kilotakip.app.data.remote.dto.AuthResponse
import com.kilotakip.app.data.remote.dto.LoginRequest
import com.kilotakip.app.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    val tokenFlow: Flow<String?> = sessionManager.tokenFlow
    val userRoleFlow: Flow<String?> = sessionManager.userRoleFlow
    val userProfileFlow: Flow<UserProfile> = sessionManager.userProfileFlow

    /**
     * @param login Kullanıcı adı veya telefon numarası.
     */
    suspend fun login(login: String, password: String): NetworkResult<AuthResponse> {
        return runCatching {
            val response = apiService.login(LoginRequest(login, password))
            handleAuthResponse(response)
        }.getOrElse { NetworkResult.Error(it.message ?: "Bağlantı hatası.") }
    }

    suspend fun register(
        name: String,
        username: String,
        phone: String,
        password: String,
        birthDate: String,
        heightCm: Double,
        currentWeightKg: Double
    ): NetworkResult<AuthResponse> {
        return runCatching {
            val response = apiService.register(
                RegisterRequest(
                    name = name,
                    username = username,
                    phone = phone,
                    password = password,
                    birth_date = birthDate,
                    height_cm = heightCm,
                    current_weight_kg = currentWeightKg
                )
            )
            handleAuthResponse(response)
        }.getOrElse { NetworkResult.Error(it.message ?: "Bağlantı hatası.") }
    }

    private suspend fun handleAuthResponse(response: retrofit2.Response<AuthResponse>): NetworkResult<AuthResponse> {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            sessionManager.saveSession(
                token = body.token,
                userId = body.user.id,
                role = body.user.role,
                name = body.user.name,
                targetWeightKg = body.user.target_weight_kg ?: body.health?.recommended_target_weight_kg,
                heightCm = body.user.height_cm,
                currentWeightKg = body.user.current_weight_kg,
                healthyWeightMinKg = body.user.healthy_weight_min_kg ?: body.health?.healthy_weight_min_kg,
                healthyWeightMaxKg = body.user.healthy_weight_max_kg ?: body.health?.healthy_weight_max_kg,
                bmi = body.user.bmi ?: body.health?.bmi
            )
            return NetworkResult.Success(body)
        }
        return NetworkResult.Error(errorMessage(response.code()))
    }

    private fun errorMessage(code: Int): String = when (code) {
        401 -> "Kullanıcı adı/telefon veya şifre hatalı."
        403 -> "Hesabınıza erişim engellendi."
        422 -> "Girdiğiniz bilgileri kontrol edin."
        else -> "Bir hata oluştu ($code)."
    }

    suspend fun logout() {
        runCatching { apiService.logout() }
        sessionManager.clearSession()
    }

    suspend fun isLoggedIn(): Boolean = sessionManager.getToken() != null
}
