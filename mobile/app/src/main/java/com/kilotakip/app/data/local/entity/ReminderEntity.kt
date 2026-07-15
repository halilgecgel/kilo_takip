package com.kilotakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val clientUuid: String,
    val serverId: Long? = null,
    val type: String,
    val title: String,
    val daysOfWeek: String,
    val startTime: String,
    val endTime: String? = null,
    val intervalMinutes: Int? = null,
    val isActive: Boolean = true,
    val syncStatus: String = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)
