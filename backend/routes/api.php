<?php

use App\Http\Controllers\Api\Admin\BlacklistedIpController;
use App\Http\Controllers\Api\Admin\UserController as AdminUserController;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\ReminderController;
use App\Http\Controllers\Api\ReminderLogController;
use App\Http\Controllers\Api\WeightEntryController;
use Illuminate\Support\Facades\Route;

Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

Route::middleware(['auth:sanctum', 'active'])->group(function () {
    Route::get('/me', [AuthController::class, 'me']);
    Route::put('/me', [AuthController::class, 'updateProfile']);
    Route::post('/logout', [AuthController::class, 'logout']);

    Route::apiResource('weight-entries', WeightEntryController::class)->except(['show']);
    Route::apiResource('reminders', ReminderController::class)->except(['show']);
    Route::get('/reminder-logs', [ReminderLogController::class, 'index']);
    Route::post('/reminder-logs/batch', [ReminderLogController::class, 'store']);

    Route::middleware('admin')->prefix('admin')->group(function () {
        Route::get('/users', [AdminUserController::class, 'index']);
        Route::get('/users/{user}', [AdminUserController::class, 'show']);
        Route::put('/users/{user}', [AdminUserController::class, 'update']);
        Route::post('/users/{user}/end-sessions', [AdminUserController::class, 'endSessions']);

        Route::apiResource('blacklisted-ips', BlacklistedIpController::class)->only(['index', 'store', 'destroy']);
    });
});
