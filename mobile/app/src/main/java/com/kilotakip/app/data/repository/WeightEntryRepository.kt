package com.kilotakip.app.data.repository

import com.kilotakip.app.data.local.dao.WeightEntryDao
import com.kilotakip.app.data.local.entity.SyncStatus
import com.kilotakip.app.data.local.entity.WeightEntryEntity
import com.kilotakip.app.data.remote.ApiService
import com.kilotakip.app.data.remote.dto.WeightEntryDto
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightEntryRepository @Inject constructor(
    private val dao: WeightEntryDao,
    private val apiService: ApiService
) {
    fun observeAll(): Flow<List<WeightEntryEntity>> = dao.observeAll()

    /**
     * Kaydı önce yerelde saklar (offline-first). WorkManager senkron görevi
     * `syncStatus = pending` olan kayıtları arka planda sunucuya gönderir.
     */
    suspend fun addEntry(weightKg: Double, note: String?, recordedAt: Long) {
        val entity = WeightEntryEntity(
            clientUuid = UUID.randomUUID().toString(),
            weightKg = weightKg,
            note = note,
            recordedAt = recordedAt,
            syncStatus = SyncStatus.PENDING
        )
        dao.upsert(entity)
    }

    suspend fun deleteEntry(clientUuid: String) {
        dao.markDeleted(clientUuid)
    }

    /**
     * Senkronize edilmemiş kayıtları sunucuya gönderir. Ağ hatasında kayıt
     * `pending` kalır ve bir sonraki denemede tekrar gönderilir.
     */
    suspend fun syncPending() {
        val pending = dao.getPendingSync()
        for (entry in pending) {
            runCatching {
                val response = apiService.createWeightEntry(entry.toDto())
                if (response.isSuccessful) {
                    response.body()?.id?.let { serverId ->
                        dao.markSynced(entry.clientUuid, serverId)
                    }
                }
            }
        }
    }

    private fun WeightEntryEntity.toDto(): WeightEntryDto = WeightEntryDto(
        client_uuid = clientUuid,
        weight_kg = weightKg,
        note = note,
        recorded_at = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(recordedAt).atZone(ZoneOffset.UTC))
    )
}
