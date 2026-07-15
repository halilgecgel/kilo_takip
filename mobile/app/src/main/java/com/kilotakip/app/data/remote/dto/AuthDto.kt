package com.kilotakip.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val username: String,
    val phone: String,
    val password: String,
    val birth_date: String,
    val height_cm: Double,
    val current_weight_kg: Double,
    val email: String? = null
)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val token: String,
    val health: HealthSummaryDto? = null
)

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val username: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val role: String,
    val status: String,
    val birth_date: String? = null,
    val height_cm: Double? = null,
    val current_weight_kg: Double? = null,
    val target_weight_kg: Double? = null,
    val age: Int? = null,
    val bmi: Double? = null,
    val healthy_weight_min_kg: Double? = null,
    val healthy_weight_max_kg: Double? = null,
    val last_login_at: String? = null
)

/**
 * Kayıt/giriş sonrası dönen, boy-kilo endeksine (VKİ) göre sağlıklı kilo özeti.
 */
@Serializable
data class HealthSummaryDto(
    val age: Int? = null,
    val bmi: Double? = null,
    val bmi_category: String? = null,
    val healthy_weight_min_kg: Double? = null,
    val healthy_weight_max_kg: Double? = null,
    val recommended_target_weight_kg: Double? = null
)
