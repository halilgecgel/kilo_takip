package com.kilotakip.app.data.repository

import com.kilotakip.app.data.remote.ApiService
import com.kilotakip.app.data.remote.dto.AdminUserDto
import com.kilotakip.app.data.remote.dto.BlacklistedIpDto
import com.kilotakip.app.data.remote.dto.UpdateUserStatusRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUsers(): List<AdminUserDto> {
        val response = apiService.getUsers()
        return if (response.isSuccessful) response.body()?.data ?: emptyList() else emptyList()
    }

    suspend fun banUser(userId: Long, banned: Boolean) {
        apiService.updateUser(userId, UpdateUserStatusRequest(status = if (banned) "banned" else "active"))
    }

    suspend fun endSessions(userId: Long) {
        apiService.endUserSessions(userId)
    }

    suspend fun getBlacklistedIps(): List<BlacklistedIpDto> {
        val response = apiService.getBlacklistedIps()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    suspend fun addBlacklistedIp(ip: String, reason: String?) {
        apiService.addBlacklistedIp(BlacklistedIpDto(ip_address = ip, reason = reason))
    }

    suspend fun removeBlacklistedIp(id: Long) {
        apiService.removeBlacklistedIp(id)
    }
}
