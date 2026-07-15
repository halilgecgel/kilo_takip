<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ReminderLog;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ReminderLogController extends Controller
{
    public function index(Request $request)
    {
        $logs = ReminderLog::where('user_id', $request->user()->id)
            ->orderByDesc('scheduled_at')
            ->paginate(50);

        return response()->json($logs);
    }

    /**
     * Mobil uygulama, bildirim onaylandığında/kaçırıldığında bu endpoint'e
     * tek tek ya da offline kuyruktan toplu (batch) gönderim yapar.
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'entries' => ['required', 'array', 'min:1'],
            'entries.*.reminder_id' => ['required', 'integer', 'exists:reminders,id'],
            'entries.*.client_uuid' => ['nullable', 'string', 'max:36'],
            'entries.*.scheduled_at' => ['required', 'date'],
            'entries.*.status' => ['required', 'in:confirmed,missed,snoozed'],
            'entries.*.responded_at' => ['nullable', 'date'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $created = [];

        foreach ($validator->validated()['entries'] as $entry) {
            $created[] = ReminderLog::updateOrCreate(
                [
                    'user_id' => $request->user()->id,
                    'client_uuid' => $entry['client_uuid'] ?? null,
                ],
                [
                    'reminder_id' => $entry['reminder_id'],
                    'scheduled_at' => $entry['scheduled_at'],
                    'status' => $entry['status'],
                    'responded_at' => $entry['responded_at'] ?? null,
                ]
            );
        }

        return response()->json($created, 201);
    }
}
