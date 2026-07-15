package com.kilotakip.app.data.remote

import com.kilotakip.app.data.remote.dto.AdminUserDto
import com.kilotakip.app.data.remote.dto.AuthResponse
import com.kilotakip.app.data.remote.dto.BlacklistedIpDto
import com.kilotakip.app.data.remote.dto.LoginRequest
import com.kilotakip.app.data.remote.dto.PaginatedUsers
import com.kilotakip.app.data.remote.dto.RegisterRequest
import com.kilotakip.app.data.remote.dto.ReminderDto
import com.kilotakip.app.data.remote.dto.ReminderLogBatchRequest
import com.kilotakip.app.data.remote.dto.UpdateUserStatusRequest
import com.kilotakip.app.data.remote.dto.UserDto
import com.kilotakip.app.data.remote.dto.WeightEntryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("me")
    suspend fun me(): Response<UserDto>

    @GET("weight-entries")
    suspend fun getWeightEntries(): Response<List<WeightEntryDto>>

    @POST("weight-entries")
    suspend fun createWeightEntry(@Body entry: WeightEntryDto): Response<WeightEntryDto>

    @PUT("weight-entries/{id}")
    suspend fun updateWeightEntry(@Path("id") id: Long, @Body entry: WeightEntryDto): Response<WeightEntryDto>

    @DELETE("weight-entries/{id}")
    suspend fun deleteWeightEntry(@Path("id") id: Long): Response<Unit>

    @GET("reminders")
    suspend fun getReminders(): Response<List<ReminderDto>>

    @POST("reminders")
    suspend fun createReminder(@Body reminder: ReminderDto): Response<ReminderDto>

    @PUT("reminders/{id}")
    suspend fun updateReminder(@Path("id") id: Long, @Body reminder: ReminderDto): Response<ReminderDto>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: Long): Response<Unit>

    @POST("reminder-logs/batch")
    suspend fun sendReminderLogs(@Body request: ReminderLogBatchRequest): Response<Unit>

    @GET("admin/users")
    suspend fun getUsers(): Response<PaginatedUsers>

    @PUT("admin/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body request: UpdateUserStatusRequest): Response<AdminUserDto>

    @POST("admin/users/{id}/end-sessions")
    suspend fun endUserSessions(@Path("id") id: Long): Response<Unit>

    @GET("admin/blacklisted-ips")
    suspend fun getBlacklistedIps(): Response<List<BlacklistedIpDto>>

    @POST("admin/blacklisted-ips")
    suspend fun addBlacklistedIp(@Body request: BlacklistedIpDto): Response<BlacklistedIpDto>

    @DELETE("admin/blacklisted-ips/{id}")
    suspend fun removeBlacklistedIp(@Path("id") id: Long): Response<Unit>
}
