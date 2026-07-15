<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use App\Support\HealthCalculator;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'name',
        'username',
        'phone',
        'email',
        'password',
        'role',
        'status',
        'birth_date',
        'height_cm',
        'current_weight_kg',
        'target_weight_kg',
        'last_login_ip',
        'last_login_at',
    ];

    /**
     * API cevaplarına otomatik olarak eklenen türetilmiş alanlar.
     *
     * @var array<int, string>
     */
    protected $appends = [
        'age',
        'bmi',
        'healthy_weight_min_kg',
        'healthy_weight_max_kg',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'birth_date' => 'date',
            'last_login_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    public function isAdmin(): bool
    {
        return $this->role === 'admin';
    }

    /**
     * Yaş, doğum tarihinden anlık hesaplanır; veritabanında saklanmaz.
     */
    public function getAgeAttribute(): ?int
    {
        return HealthCalculator::calculateAge($this->birth_date?->toDateString());
    }

    public function getBmiAttribute(): ?float
    {
        if (! $this->height_cm || ! $this->current_weight_kg) {
            return null;
        }

        return HealthCalculator::calculateBmi((float) $this->current_weight_kg, (float) $this->height_cm);
    }

    public function getHealthyWeightMinKgAttribute(): ?float
    {
        if (! $this->height_cm) {
            return null;
        }

        return HealthCalculator::healthyWeightRange((float) $this->height_cm)['min'];
    }

    public function getHealthyWeightMaxKgAttribute(): ?float
    {
        if (! $this->height_cm) {
            return null;
        }

        return HealthCalculator::healthyWeightRange((float) $this->height_cm)['max'];
    }

    public function weightEntries(): HasMany
    {
        return $this->hasMany(WeightEntry::class);
    }

    public function reminders(): HasMany
    {
        return $this->hasMany(Reminder::class);
    }

    public function loginLogs(): HasMany
    {
        return $this->hasMany(LoginLog::class);
    }

    public function devices(): HasMany
    {
        return $this->hasMany(Device::class);
    }
}
