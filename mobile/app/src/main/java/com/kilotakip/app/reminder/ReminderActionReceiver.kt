package com.kilotakip.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kilotakip.app.data.local.entity.ReminderLogStatus
import com.kilotakip.app.data.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Bildirimdeki "Yaptım" aksiyonuna basıldığında hatırlatıcı durumunu onaylı olarak günceller. */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val reminderUuid = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_UUID) ?: return
        val scheduledAt = intent.getLongExtra(ReminderScheduler.EXTRA_SCHEDULED_AT, System.currentTimeMillis())

        if (intent.action == ACTION_CONFIRM) {
            CoroutineScope(Dispatchers.IO).launch {
                reminderRepository.recordReminderResponse(reminderUuid, scheduledAt, ReminderLogStatus.CONFIRMED)
            }
        }

        NotificationManagerCompat.from(context).cancel(reminderUuid.hashCode())
    }

    companion object {
        const val ACTION_CONFIRM = "com.kilotakip.app.ACTION_CONFIRM_REMINDER"
    }
}
