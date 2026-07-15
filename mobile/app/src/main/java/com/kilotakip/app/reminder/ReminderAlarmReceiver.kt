package com.kilotakip.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kilotakip.app.KiloTakipApp
import com.kilotakip.app.MainActivity
import com.kilotakip.app.data.local.dao.ReminderDao
import com.kilotakip.app.data.local.entity.ReminderLogStatus
import com.kilotakip.app.data.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Bir hatırlatıcı alarmı tetiklendiğinde çalışır: onaylanabilir bir bildirim gösterir,
 * kaçırılmış say ilan eder (kullanıcı onaylamazsa bu durum kalıcı olur) ve bir sonraki
 * tetiklenmeyi otomatik olarak yeniden kurar.
 */
@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var reminderDao: ReminderDao
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val reminderUuid = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_UUID) ?: return
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Hatırlatıcı"
        val scheduledAt = intent.getLongExtra(ReminderScheduler.EXTRA_SCHEDULED_AT, System.currentTimeMillis())

        showNotification(context, reminderUuid, title, scheduledAt)

        CoroutineScope(Dispatchers.IO).launch {
            // Kullanıcı onaylamazsa varsayılan durum "missed" (yapılmamış sayılır).
            reminderRepository.recordReminderResponse(reminderUuid, scheduledAt, ReminderLogStatus.MISSED)

            val reminder = reminderDao.getActive().firstOrNull { it.clientUuid == reminderUuid }
            if (reminder != null) {
                reminderScheduler.scheduleNext(reminder, LocalDateTime.now())
            }
        }
    }

    private fun showNotification(context: Context, reminderUuid: String, title: String, scheduledAt: Long) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = android.app.PendingIntent.getActivity(
            context, reminderUuid.hashCode(), contentIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val confirmIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_CONFIRM
            putExtra(ReminderScheduler.EXTRA_REMINDER_UUID, reminderUuid)
            putExtra(ReminderScheduler.EXTRA_SCHEDULED_AT, scheduledAt)
        }
        val confirmPendingIntent = android.app.PendingIntent.getBroadcast(
            context, (reminderUuid + "confirm").hashCode(), confirmIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, KiloTakipApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText("Yapıldığını onaylamazsan yapılmamış sayılır.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Yaptım ✓", confirmPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(reminderUuid.hashCode(), notification)
    }
}
