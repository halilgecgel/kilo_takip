<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('username')->nullable()->after('name');
            $table->string('phone', 20)->nullable()->after('username');
            $table->date('birth_date')->nullable()->after('status');
            $table->decimal('current_weight_kg', 5, 1)->nullable()->after('birth_date');
        });

        // E-posta artık zorunlu değil (kullanıcı adı/telefon ile kayıt/giriş yapılabiliyor).
        if (DB::getDriverName() === 'mysql') {
            DB::statement('ALTER TABLE users MODIFY email VARCHAR(255) NULL');
        }

        // Mevcut (varsa) kayıtlar için benzersiz kullanıcı adı/telefon üretilir.
        DB::table('users')->whereNull('username')->orWhereNull('phone')->orderBy('id')->get()->each(function ($user) {
            DB::table('users')->where('id', $user->id)->update([
                'username' => 'user'.$user->id,
                'phone' => '00000'.str_pad((string) $user->id, 5, '0', STR_PAD_LEFT),
            ]);
        });

        Schema::table('users', function (Blueprint $table) {
            $table->unique('username');
            $table->unique('phone');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropUnique(['username']);
            $table->dropUnique(['phone']);
            $table->dropColumn(['username', 'phone', 'birth_date', 'current_weight_kg']);
        });
    }
};
