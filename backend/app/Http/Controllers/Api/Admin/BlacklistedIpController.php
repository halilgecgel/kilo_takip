<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\BlacklistedIp;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class BlacklistedIpController extends Controller
{
    public function index()
    {
        return response()->json(BlacklistedIp::orderByDesc('created_at')->get());
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'ip_address' => ['required', 'ip', 'unique:blacklisted_ips,ip_address'],
            'reason' => ['nullable', 'string', 'max:255'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $ip = BlacklistedIp::create([
            ...$validator->validated(),
            'created_by' => $request->user()->id,
        ]);

        return response()->json($ip, 201);
    }

    public function destroy(BlacklistedIp $blacklistedIp)
    {
        $blacklistedIp->delete();

        return response()->json(['message' => 'IP kara listeden çıkarıldı.']);
    }
}
