package com.kilotakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kilotakip.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE isDeleted = 0 ORDER BY startTime ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isDeleted = 0 AND isActive = 1")
    suspend fun getActive(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE syncStatus != 'synced'")
    suspend fun getPendingSync(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders SET syncStatus = :status, serverId = :serverId WHERE clientUuid = :clientUuid")
    suspend fun markSynced(clientUuid: String, serverId: Long, status: String = "synced")

    @Query("UPDATE reminders SET isDeleted = 1, syncStatus = 'pending' WHERE clientUuid = :clientUuid")
    suspend fun markDeleted(clientUuid: String)
}
