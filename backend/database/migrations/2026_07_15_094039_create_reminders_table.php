<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('reminders', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->uuid('client_uuid')->nullable()->index();
            $table->enum('type', ['su', 'ilac', 'hareket', 'ozel'])->default('ozel');
            $table->string('title');
            $table->string('days_of_week');
            $table->time('start_time');
            $table->time('end_time')->nullable();
            $table->unsignedInteger('interval_minutes')->nullable();
            $table->boolean('is_active')->default(true);
            $table->timestamps();

            $table->unique(['user_id', 'client_uuid']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('reminders');
    }
};
