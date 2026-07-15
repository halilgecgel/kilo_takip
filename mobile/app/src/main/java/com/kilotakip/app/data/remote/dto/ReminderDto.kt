package com.kilotakip.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReminderDto(
    val id: Long? = null,
    val client_uuid: String? = null,
    val type: String,
    val title: String,
    val days_of_week: String,
    val start_time: String,
    val end_time: String? = null,
    val interval_minutes: Int? = null,
    val is_active: Boolean = true
)

@Serializable
data class ReminderLogEntryDto(
    val reminder_id: Long,
    val client_uuid: String? = null,
    val scheduled_at: String,
    val status: String,
    val responded_at: String? = null
)

@Serializable
data class ReminderLogBatchRequest(
    val entries: List<ReminderLogEntryDto>
)
