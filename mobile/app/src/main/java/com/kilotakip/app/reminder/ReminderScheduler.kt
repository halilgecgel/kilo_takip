package com.kilotakip.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kilotakip.app.data.local.entity.ReminderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bir hatırlatıcı için "days_of_week" (1=Pazartesi..7=Pazar CSV), start/end_time ve
 * opsiyonel interval_minutes bilgisine göre bir sonraki tetiklenme anını hesaplayıp
 * AlarmManager ile tam zamanlı (exact) alarm kurar. Alarm tetiklendiğinde
 * [ReminderAlarmReceiver] bir sonraki alarmı otomatik olarak yeniden kurar; böylece
 * gün değişimi ve periyodik tekrarlar sürekli olarak yönetilir.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNext(reminder: ReminderEntity, after: LocalDateTime = LocalDateTime.now()) {
        if (!reminder.isActive) return

        val nextTrigger = computeNextTrigger(reminder, after) ?: return
        val triggerMillis = nextTrigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_UUID, reminder.clientUuid)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_TYPE, reminder.type)
            putExtra(EXTRA_SCHEDULED_AT, triggerMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.clientUuid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }

    fun cancel(reminder: ReminderEntity) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.clientUuid.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * "days_of_week" alanında bugünden başlayarak, verilen zamandan sonraki en yakın
     * geçerli tetiklenme anını (start/end/interval bilgisine göre) bulur.
     */
    private fun computeNextTrigger(reminder: ReminderEntity, after: LocalDateTime): LocalDateTime? {
        val activeDays = reminder.daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        if (activeDays.isEmpty()) return null

        val startTime = runCatching { LocalTime.parse(reminder.startTime) }.getOrNull() ?: return null
        val endTime = reminder.endTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        val intervalMinutes = reminder.intervalMinutes?.takeIf { it > 0 }

        for (dayOffset in 0..7) {
            val date = after.toLocalDate().plusDays(dayOffset.toLong())
            if (date.dayOfWeek.isoValueIn(activeDays).not()) continue

            val candidates = buildDayCandidates(date, startTime, endTime, intervalMinutes)
            val next = candidates.firstOrNull { it.isAfter(after) }
            if (next != null) return next
        }
        return null
    }

    private fun buildDayCandidates(
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime?,
        intervalMinutes: Int?
    ): List<LocalDateTime> {
        if (intervalMinutes == null || endTime == null) {
            return listOf(LocalDateTime.of(date, startTime))
        }

        val times = mutableListOf<LocalDateTime>()
        var current = LocalDateTime.of(date, startTime)
        val end = LocalDateTime.of(date, endTime)
        while (!current.isAfter(end)) {
            times.add(current)
            current = current.plusMinutes(intervalMinutes.toLong())
        }
        return times
    }

    private fun DayOfWeek.isoValueIn(days: Set<Int>): Boolean = value in days

    companion object {
        const val EXTRA_REMINDER_UUID = "reminder_uuid"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TYPE = "type"
        const val EXTRA_SCHEDULED_AT = "scheduled_at"
    }
}
