<?php

namespace App\Http\Middleware;

use App\Models\BlacklistedIp;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureIpNotBlacklisted
{
    public function handle(Request $request, Closure $next): Response
    {
        $ip = $request->ip();

        if (BlacklistedIp::where('ip_address', $ip)->exists()) {
            return response()->json([
                'message' => 'Bu IP adresi engellenmiştir.',
            ], 403);
        }

        return $next($request);
    }
}
