<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Reminder;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ReminderController extends Controller
{
    public function index(Request $request)
    {
        return response()->json($request->user()->reminders()->orderBy('start_time')->get());
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), $this->rules());

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $reminder = $request->user()->reminders()->updateOrCreate(
            ['client_uuid' => $request->client_uuid],
            $validator->validated()
        );

        return response()->json($reminder, 201);
    }

    public function update(Request $request, Reminder $reminder)
    {
        $this->authorizeOwnership($request, $reminder);

        $validator = Validator::make($request->all(), $this->rules(sometimes: true));

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $reminder->update($validator->validated());

        return response()->json($reminder);
    }

    public function destroy(Request $request, Reminder $reminder)
    {
        $this->authorizeOwnership($request, $reminder);

        $reminder->delete();

        return response()->json(['message' => 'Hatırlatıcı silindi.']);
    }

    private function rules(bool $sometimes = false): array
    {
        $rule = $sometimes ? 'sometimes' : 'required';

        return [
            'client_uuid' => ['nullable', 'string', 'max:36'],
            'type' => [$rule, 'in:su,ilac,hareket,ozel'],
            'title' => [$rule, 'string', 'max:255'],
            'days_of_week' => [$rule, 'string'],
            'start_time' => [$rule, 'date_format:H:i'],
            'end_time' => ['nullable', 'date_format:H:i'],
            'interval_minutes' => ['nullable', 'integer', 'min:1'],
            'is_active' => ['sometimes', 'boolean'],
        ];
    }

    private function authorizeOwnership(Request $request, Reminder $reminder): void
    {
        abort_if($reminder->user_id !== $request->user()->id, 403, 'Bu hatırlatıcıya erişim yetkiniz yok.');
    }
}
