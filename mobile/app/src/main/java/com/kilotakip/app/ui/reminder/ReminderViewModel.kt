package com.kilotakip.app.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilotakip.app.data.local.entity.ReminderEntity
import com.kilotakip.app.data.repository.ReminderRepository
import com.kilotakip.app.reminder.ReminderScheduler
import com.kilotakip.app.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = reminderRepository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(
        type: String,
        title: String,
        daysOfWeek: Set<Int>,
        startTime: String,
        endTime: String?,
        intervalMinutes: Int?
    ) {
        viewModelScope.launch {
            val reminder = reminderRepository.addReminder(
                type = type,
                title = title,
                daysOfWeek = daysOfWeek.sorted().joinToString(","),
                startTime = startTime,
                endTime = endTime,
                intervalMinutes = intervalMinutes
            )
            reminderScheduler.scheduleNext(reminder)
            syncScheduler.syncNow()
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderScheduler.cancel(reminder)
            reminderRepository.deleteReminder(reminder.clientUuid)
            syncScheduler.syncNow()
        }
    }
}
