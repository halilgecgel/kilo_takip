package com.kilotakip.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "session")

data class UserProfile(
    val name: String = "",
    val targetWeightKg: Double? = null,
    val heightCm: Double? = null,
    val currentWeightKg: Double? = null,
    val healthyWeightMinKg: Double? = null,
    val healthyWeightMaxKg: Double? = null,
    val bmi: Double? = null
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val userRoleKey = stringPreferencesKey("user_role")
    private val userNameKey = stringPreferencesKey("user_name")
    private val targetWeightKey = doublePreferencesKey("target_weight_kg")
    private val heightKey = doublePreferencesKey("height_cm")
    private val currentWeightKey = doublePreferencesKey("current_weight_kg")
    private val healthyMinKey = doublePreferencesKey("healthy_weight_min_kg")
    private val healthyMaxKey = doublePreferencesKey("healthy_weight_max_kg")
    private val bmiKey = doublePreferencesKey("bmi")

    val tokenFlow: Flow<String?> = context.sessionDataStore.data.map { it[tokenKey] }
    val userRoleFlow: Flow<String?> = context.sessionDataStore.data.map { it[userRoleKey] }
    val userProfileFlow: Flow<UserProfile> = context.sessionDataStore.data.map { prefs ->
        UserProfile(
            name = prefs[userNameKey] ?: "",
            targetWeightKg = prefs[targetWeightKey],
            heightCm = prefs[heightKey],
            currentWeightKg = prefs[currentWeightKey],
            healthyWeightMinKg = prefs[healthyMinKey],
            healthyWeightMaxKg = prefs[healthyMaxKey],
            bmi = prefs[bmiKey]
        )
    }

    suspend fun getToken(): String? = context.sessionDataStore.data.first()[tokenKey]

    suspend fun saveSession(
        token: String,
        userId: Long,
        role: String,
        name: String = "",
        targetWeightKg: Double? = null,
        heightCm: Double? = null,
        currentWeightKg: Double? = null,
        healthyWeightMinKg: Double? = null,
        healthyWeightMaxKg: Double? = null,
        bmi: Double? = null
    ) {
        context.sessionDataStore.edit {
            it[tokenKey] = token
            it[userIdKey] = userId.toString()
            it[userRoleKey] = role
            it[userNameKey] = name
            targetWeightKg?.let { v -> it[targetWeightKey] = v }
            heightCm?.let { v -> it[heightKey] = v }
            currentWeightKg?.let { v -> it[currentWeightKey] = v }
            healthyWeightMinKg?.let { v -> it[healthyMinKey] = v }
            healthyWeightMaxKg?.let { v -> it[healthyMaxKey] = v }
            bmi?.let { v -> it[bmiKey] = v }
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }
}
