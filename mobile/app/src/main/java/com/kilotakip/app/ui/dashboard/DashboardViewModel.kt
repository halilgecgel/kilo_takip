package com.kilotakip.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilotakip.app.data.datastore.UserProfile
import com.kilotakip.app.data.local.entity.WeightEntryEntity
import com.kilotakip.app.data.repository.AuthRepository
import com.kilotakip.app.data.repository.WeightEntryRepository
import com.kilotakip.app.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class DashboardUiState(
    val entries: List<WeightEntryEntity> = emptyList(),
    val lastWeight: Double? = null,
    val weightChange: Double? = null,
    val motivationalMessage: String = "Bugün de harika gidiyorsun!",
    val userName: String = "",
    val targetWeightKg: Double? = null,
    val healthyWeightMinKg: Double? = null,
    val healthyWeightMaxKg: Double? = null,
    val progressPercent: Float = 0f,
    val remainingKg: Double? = null,
    val startWeightKg: Double? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weightEntryRepository: WeightEntryRepository,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        weightEntryRepository.observeAll(),
        authRepository.userProfileFlow
    ) { entries, profile ->
        buildUiState(entries, profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        syncScheduler.syncNow()
    }

    private fun buildUiState(entries: List<WeightEntryEntity>, profile: UserProfile): DashboardUiState {
        val sorted = entries.sortedByDescending { it.recordedAt }
        val last = sorted.firstOrNull()?.weightKg
        val previous = sorted.getOrNull(1)?.weightKg
        val change = if (last != null && previous != null) last - previous else null

        val target = profile.targetWeightKg
        val startWeight = profile.currentWeightKg
        val currentWeight = last ?: startWeight

        val remaining = if (currentWeight != null && target != null) currentWeight - target else null
        val progress = if (startWeight != null && target != null && currentWeight != null && startWeight != target) {
            val totalToLose = startWeight - target
            val lost = startWeight - currentWeight
            (lost / totalToLose).toFloat().coerceIn(0f, 1f)
        } else 0f

        val message = when {
            change == null -> "İlk kaydını ekleyerek yolculuğuna başla!"
            remaining != null && abs(remaining) < 1.0 -> "Hedefe neredeyse ulaştın! Son hamle!"
            change < -0.5 -> "Harika gidiyorsun, ${"%.1f".format(-change)} kg verdin!"
            change < 0 -> "Güzel ilerleme, devam et!"
            change > 0.5 -> "Küçük bir geri adım, pes etme!"
            change > 0 -> "Hafif bir artış, bugün telafi günü!"
            else -> "İstikrar da bir başarıdır!"
        }

        return DashboardUiState(
            entries = sorted,
            lastWeight = last,
            weightChange = change,
            motivationalMessage = message,
            userName = profile.name,
            targetWeightKg = target,
            healthyWeightMinKg = profile.healthyWeightMinKg,
            healthyWeightMaxKg = profile.healthyWeightMaxKg,
            progressPercent = progress,
            remainingKg = remaining,
            startWeightKg = startWeight
        )
    }

    fun addWeight(weightKg: Double, note: String?) {
        viewModelScope.launch {
            weightEntryRepository.addEntry(weightKg, note, System.currentTimeMillis())
            syncScheduler.syncNow()
        }
    }

    fun deleteEntry(clientUuid: String) {
        viewModelScope.launch {
            weightEntryRepository.deleteEntry(clientUuid)
            syncScheduler.syncNow()
        }
    }
}
