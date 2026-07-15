package com.kilotakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_logs")
data class ReminderLogEntity(
    @PrimaryKey val clientUuid: String,
    val reminderClientUuid: String,
    val reminderServerId: Long? = null,
    val scheduledAt: Long,
    val status: String,
    val respondedAt: Long? = null,
    val syncStatus: String = SyncStatus.PENDING
)

object ReminderLogStatus {
    const val CONFIRMED = "confirmed"
    const val MISSED = "missed"
    const val SNOOZED = "snoozed"
}
