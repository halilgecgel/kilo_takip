package com.kilotakip.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.kilotakip.app.data.local.AppDatabase
import com.kilotakip.app.data.local.dao.ReminderDao
import com.kilotakip.app.data.local.dao.ReminderLogDao
import com.kilotakip.app.data.local.dao.WeightEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "kilo_takip.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWeightEntryDao(db: AppDatabase): WeightEntryDao = db.weightEntryDao()

    @Provides
    fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideReminderLogDao(db: AppDatabase): ReminderLogDao = db.reminderLogDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
