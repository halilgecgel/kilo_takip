package com.kilotakip.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kilotakip.app.data.local.dao.ReminderDao
import com.kilotakip.app.data.local.dao.ReminderLogDao
import com.kilotakip.app.data.local.dao.WeightEntryDao
import com.kilotakip.app.data.local.entity.ReminderEntity
import com.kilotakip.app.data.local.entity.ReminderLogEntity
import com.kilotakip.app.data.local.entity.WeightEntryEntity

@Database(
    entities = [WeightEntryEntity::class, ReminderEntity::class, ReminderLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderLogDao(): ReminderLogDao
}
