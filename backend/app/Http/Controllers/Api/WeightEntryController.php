<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\WeightEntry;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class WeightEntryController extends Controller
{
    public function index(Request $request)
    {
        $entries = $request->user()
            ->weightEntries()
            ->orderByDesc('recorded_at')
            ->get();

        return response()->json($entries);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'client_uuid' => ['nullable', 'string', 'max:36'],
            'weight_kg' => ['required', 'numeric', 'min:1', 'max:500'],
            'note' => ['nullable', 'string', 'max:255'],
            'recorded_at' => ['required', 'date'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        // client_uuid ile offline senkron sonrası tekrar gönderilen kayıtlar için upsert yapılır.
        $entry = $request->user()->weightEntries()->updateOrCreate(
            ['client_uuid' => $request->client_uuid],
            $validator->validated()
        );

        return response()->json($entry, 201);
    }

    public function update(Request $request, WeightEntry $weightEntry)
    {
        $this->authorizeOwnership($request, $weightEntry);

        $validator = Validator::make($request->all(), [
            'weight_kg' => ['sometimes', 'numeric', 'min:1', 'max:500'],
            'note' => ['sometimes', 'nullable', 'string', 'max:255'],
            'recorded_at' => ['sometimes', 'date'],
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Doğrulama hatası.', 'errors' => $validator->errors()], 422);
        }

        $weightEntry->update($validator->validated());

        return response()->json($weightEntry);
    }

    public function destroy(Request $request, WeightEntry $weightEntry)
    {
        $this->authorizeOwnership($request, $weightEntry);

        $weightEntry->delete();

        return response()->json(['message' => 'Kayıt silindi.']);
    }

    private function authorizeOwnership(Request $request, WeightEntry $weightEntry): void
    {
        abort_if($weightEntry->user_id !== $request->user()->id, 403, 'Bu kayda erişim yetkiniz yok.');
    }
}
