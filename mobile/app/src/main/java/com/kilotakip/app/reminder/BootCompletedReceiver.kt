package com.kilotakip.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kilotakip.app.data.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Cihaz yeniden başladığında AlarmManager alarmları sıfırlanır; burada yeniden kurulur. */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            reminderRepository.getActiveReminders().forEach { reminder ->
                reminderScheduler.scheduleNext(reminder)
            }
        }
    }
}
