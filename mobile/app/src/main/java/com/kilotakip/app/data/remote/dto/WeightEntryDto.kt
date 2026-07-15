package com.kilotakip.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightEntryDto(
    val id: Long? = null,
    val client_uuid: String? = null,
    val weight_kg: Double,
    val note: String? = null,
    val recorded_at: String
)
