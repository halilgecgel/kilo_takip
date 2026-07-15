package com.kilotakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kilotakip.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Query("SELECT * FROM weight_entries WHERE isDeleted = 0 ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE syncStatus != 'synced'")
    suspend fun getPendingSync(): List<WeightEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<WeightEntryEntity>)

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Query("UPDATE weight_entries SET syncStatus = :status, serverId = :serverId WHERE clientUuid = :clientUuid")
    suspend fun markSynced(clientUuid: String, serverId: Long, status: String = "synced")

    @Query("UPDATE weight_entries SET isDeleted = 1, syncStatus = 'pending' WHERE clientUuid = :clientUuid")
    suspend fun markDeleted(clientUuid: String)
}
