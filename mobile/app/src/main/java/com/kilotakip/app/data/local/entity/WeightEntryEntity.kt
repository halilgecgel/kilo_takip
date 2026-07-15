package com.kilotakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey val clientUuid: String,
    val serverId: Long? = null,
    val weightKg: Double,
    val note: String? = null,
    val recordedAt: Long,
    val syncStatus: String = SyncStatus.PENDING,
    val isDeleted: Boolean = false
)

object SyncStatus {
    const val PENDING = "pending"
    const val SYNCED = "synced"
    const val FAILED = "failed"
}
