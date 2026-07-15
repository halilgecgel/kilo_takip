package com.kilotakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kilotakip.app.data.local.entity.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {

    @Query("SELECT * FROM reminder_logs ORDER BY scheduledAt DESC")
    fun observeAll(): Flow<List<ReminderLogEntity>>

    @Query("SELECT * FROM reminder_logs WHERE syncStatus != 'synced'")
    suspend fun getPendingSync(): List<ReminderLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: ReminderLogEntity)

    @Query("UPDATE reminder_logs SET syncStatus = 'synced' WHERE clientUuid IN (:clientUuids)")
    suspend fun markSynced(clientUuids: List<String>)
}
