<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\LoginLog;
use App\Models\User;
use App\Support\HealthCalculator;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => ['required', 'string', 'max:255'],
            'username' => ['required', 'string', 'max:255', 'alpha_dash', 'unique:users,username'],
            'phone' => ['required', 'string', 'max:20', 'unique:users,phone'],
            'email' => ['nullable', 'string', 'email', 'max:255', 'unique:users,email'],
            'password' => ['required', 'string', 'min:6'],
            'birth_date' => ['required', 'date', 'before:today'],
            'height_cm' => ['required', 'numeric', 'min:50', 'max:250'],
            'current_weight_kg' => ['required', 'numeric', 'min:20', 'max:400'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $data = $validator->validated();

        // Boy-kilo endeksine (VKİ) göre sağlıklı olması gereken kilo hesaplanır.
        $healthyRange = HealthCalculator::healthyWeightRange((float) $data['height_cm']);

        $user = User::create([
            'name' => $data['name'],
            'username' => $data['username'],
            'phone' => $data['phone'],
            'email' => $data['email'] ?? null,
            'password' => Hash::make($data['password']),
            'birth_date' => $data['birth_date'],
            'height_cm' => $data['height_cm'],
            'current_weight_kg' => $data['current_weight_kg'],
            'target_weight_kg' => $healthyRange['target'],
            'last_login_ip' => $request->ip(),
            'last_login_at' => now(),
        ]);

        $token = $user->createToken('mobile')->plainTextToken;

        LoginLog::create([
            'user_id' => $user->id,
            'ip_address' => $request->ip(),
            'device_info' => $request->header('User-Agent'),
            'status' => 'success',
        ]);

        return response()->json([
            'user' => $user,
            'token' => $token,
            'health' => $this->healthSummary($user),
        ], 201);
    }

    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'login' => ['required', 'string'],
            'password' => ['required', 'string'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        // Kullanıcı adı veya telefon numarası ile giriş.
        $login = $request->string('login')->trim()->toString();

        $user = User::where('username', $login)
            ->orWhere('phone', $login)
            ->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            if ($user) {
                LoginLog::create([
                    'user_id' => $user->id,
                    'ip_address' => $request->ip(),
                    'device_info' => $request->header('User-Agent'),
                    'status' => 'failed',
                ]);
            }

            return response()->json(['message' => 'Kullanıcı adı/telefon veya şifre hatalı.'], 401);
        }

        if ($user->status === 'banned') {
            LoginLog::create([
                'user_id' => $user->id,
                'ip_address' => $request->ip(),
                'device_info' => $request->header('User-Agent'),
                'status' => 'blocked',
            ]);

            return response()->json(['message' => 'Hesabınız yönetici tarafından askıya alınmıştır.'], 403);
        }

        $user->forceFill([
            'last_login_ip' => $request->ip(),
            'last_login_at' => now(),
        ])->save();

        // Kalıcı oturum: aynı cihaz için önceki token'ı geçersiz kılmadan yeni bir token üretilir.
        $token = $user->createToken($request->header('User-Agent', 'mobile'))->plainTextToken;

        LoginLog::create([
            'user_id' => $user->id,
            'ip_address' => $request->ip(),
            'device_info' => $request->header('User-Agent'),
            'status' => 'success',
        ]);

        return response()->json([
            'user' => $user,
            'token' => $token,
            'health' => $this->healthSummary($user),
        ]);
    }

    public function me(Request $request)
    {
        return response()->json($request->user());
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json(['message' => 'Çıkış yapıldı.']);
    }

    public function updateProfile(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => ['sometimes', 'string', 'max:255'],
            'height_cm' => ['sometimes', 'nullable', 'numeric', 'min:50', 'max:250'],
            'current_weight_kg' => ['sometimes', 'nullable', 'numeric', 'min:20', 'max:400'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $data = $validator->validated();
        $user = $request->user();

        // Boy güncellenirse hedef (sağlıklı) kilo da yeniden hesaplanır.
        if (array_key_exists('height_cm', $data) && $data['height_cm']) {
            $data['target_weight_kg'] = HealthCalculator::healthyWeightRange((float) $data['height_cm'])['target'];
        }

        $user->update($data);

        return response()->json($user);
    }

    /**
     * Yaş (doğum tarihinden anlık), VKİ ve sağlıklı kilo aralığını içeren özet.
     */
    private function healthSummary(User $user): array
    {
        return [
            'age' => $user->age,
            'bmi' => $user->bmi,
            'bmi_category' => $user->bmi ? HealthCalculator::bmiCategory($user->bmi) : null,
            'healthy_weight_min_kg' => $user->healthy_weight_min_kg,
            'healthy_weight_max_kg' => $user->healthy_weight_max_kg,
            'recommended_target_weight_kg' => $user->target_weight_kg,
        ];
    }
}
