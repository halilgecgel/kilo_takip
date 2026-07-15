<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class UserController extends Controller
{
    public function index(Request $request)
    {
        $users = User::query()
            ->when($request->search, fn ($q) => $q->where('name', 'like', "%{$request->search}%")
                ->orWhere('username', 'like', "%{$request->search}%")
                ->orWhere('phone', 'like', "%{$request->search}%")
                ->orWhere('email', 'like', "%{$request->search}%"))
            ->orderByDesc('created_at')
            ->paginate(20);

        return response()->json($users);
    }

    public function show(User $user)
    {
        $user->load(['weightEntries' => fn ($q) => $q->orderByDesc('recorded_at')->limit(50)]);
        $user->load(['loginLogs' => fn ($q) => $q->orderByDesc('created_at')->limit(50)]);

        return response()->json($user);
    }

    public function update(Request $request, User $user)
    {
        $validator = Validator::make($request->all(), [
            'status' => ['sometimes', 'in:active,banned'],
            'role' => ['sometimes', 'in:user,admin'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $user->update($validator->validated());

        if ($user->status === 'banned') {
            $user->tokens()->delete();
        }

        return response()->json($user);
    }

    /**
     * Admin, kullanıcının aktif oturumunu (token'larını) sonlandırır.
     */
    public function endSessions(User $user)
    {
        $user->tokens()->delete();

        return response()->json(['message' => 'Kullanıcının tüm oturumları sonlandırıldı.']);
    }
}
