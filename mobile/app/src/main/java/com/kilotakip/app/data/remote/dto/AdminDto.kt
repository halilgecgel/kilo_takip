package com.kilotakip.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminUserDto(
    val id: Long,
    val name: String,
    val username: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val role: String,
    val status: String,
    val age: Int? = null,
    val last_login_ip: String? = null,
    val last_login_at: String? = null,
    val created_at: String? = null
)

@Serializable
data class PaginatedUsers(
    val data: List<AdminUserDto>
)

@Serializable
data class UpdateUserStatusRequest(
    val status: String? = null,
    val role: String? = null
)

@Serializable
data class BlacklistedIpDto(
    val id: Long? = null,
    val ip_address: String,
    val reason: String? = null
)
