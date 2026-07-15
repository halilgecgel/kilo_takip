package com.kilotakip.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kilotakip.app.data.repository.ReminderRepository
import com.kilotakip.app.data.repository.WeightEntryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * İnternet bağlantısı geldiğinde (veya periyodik olarak) yerelde bekleyen
 * kilo kayıtlarını ve hatırlatıcı verilerini sunucuya senkronize eder.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val weightEntryRepository: WeightEntryRepository,
    private val reminderRepository: ReminderRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            weightEntryRepository.syncPending()
            reminderRepository.syncPending()
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val WORK_NAME = "sync_worker"
    }
}
