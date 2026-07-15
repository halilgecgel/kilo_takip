package com.kilotakip.app.data.repository

import com.kilotakip.app.data.local.dao.ReminderDao
import com.kilotakip.app.data.local.dao.ReminderLogDao
import com.kilotakip.app.data.local.entity.ReminderEntity
import com.kilotakip.app.data.local.entity.ReminderLogEntity
import com.kilotakip.app.data.local.entity.SyncStatus
import com.kilotakip.app.data.remote.ApiService
import com.kilotakip.app.data.remote.dto.ReminderDto
import com.kilotakip.app.data.remote.dto.ReminderLogBatchRequest
import com.kilotakip.app.data.remote.dto.ReminderLogEntryDto
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reminderLogDao: ReminderLogDao,
    private val apiService: ApiService
) {
    fun observeReminders(): Flow<List<ReminderEntity>> = reminderDao.observeAll()

    fun observeLogs(): Flow<List<ReminderLogEntity>> = reminderLogDao.observeAll()

    suspend fun getActiveReminders(): List<ReminderEntity> = reminderDao.getActive()

    suspend fun addReminder(
        type: String,
        title: String,
        daysOfWeek: String,
        startTime: String,
        endTime: String?,
        intervalMinutes: Int?
    ): ReminderEntity {
        val entity = ReminderEntity(
            clientUuid = UUID.randomUUID().toString(),
            type = type,
            title = title,
            daysOfWeek = daysOfWeek,
            startTime = startTime,
            endTime = endTime,
            intervalMinutes = intervalMinutes,
            syncStatus = SyncStatus.PENDING
        )
        reminderDao.upsert(entity)
        return entity
    }

    suspend fun deleteReminder(clientUuid: String) {
        reminderDao.markDeleted(clientUuid)
    }

    /**
     * Kullanıcı bildirimi onayladığında/kaçırdığında çağrılır. Sonuç önce
     * yerelde saklanır, ardından senkron görevi sunucuya iletir.
     */
    suspend fun recordReminderResponse(reminderClientUuid: String, scheduledAt: Long, status: String) {
        reminderLogDao.upsert(
            ReminderLogEntity(
                clientUuid = UUID.randomUUID().toString(),
                reminderClientUuid = reminderClientUuid,
                scheduledAt = scheduledAt,
                status = status,
                respondedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )
        )
    }

    suspend fun syncPending() {
        syncPendingReminders()
        syncPendingLogs()
    }

    private suspend fun syncPendingReminders() {
        for (reminder in reminderDao.getPendingSync()) {
            runCatching {
                val response = apiService.createReminder(reminder.toDto())
                if (response.isSuccessful) {
                    response.body()?.id?.let { serverId ->
                        reminderDao.markSynced(reminder.clientUuid, serverId)
                    }
                }
            }
        }
    }

    private suspend fun syncPendingLogs() {
        val pending = reminderLogDao.getPendingSync()
        if (pending.isEmpty()) return

        runCatching {
            val entries = pending.map { it.toDto() }
            val response = apiService.sendReminderLogs(ReminderLogBatchRequest(entries))
            if (response.isSuccessful) {
                reminderLogDao.markSynced(pending.map { it.clientUuid })
            }
        }
    }

    private fun ReminderEntity.toDto(): ReminderDto = ReminderDto(
        client_uuid = clientUuid,
        type = type,
        title = title,
        days_of_week = daysOfWeek,
        start_time = startTime,
        end_time = endTime,
        interval_minutes = intervalMinutes,
        is_active = isActive
    )

    private fun ReminderLogEntity.toDto(): ReminderLogEntryDto = ReminderLogEntryDto(
        reminder_id = reminderServerId ?: 0,
        client_uuid = clientUuid,
        scheduled_at = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(scheduledAt).atZone(ZoneOffset.UTC)),
        status = status,
        responded_at = respondedAt?.let {
            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC))
        }
    )
}
