<?php

namespace App\Support;

use Carbon\Carbon;

/**
 * Boy/kilo endeksi (VKİ/BMI) ve doğum tarihinden yaş hesaplama yardımcıları.
 */
class HealthCalculator
{
    /** Sağlıklı VKİ aralığı referansı (WHO). */
    private const HEALTHY_BMI_MIN = 18.5;
    private const HEALTHY_BMI_MAX = 24.9;

    /**
     * VKİ (Vücut Kitle İndeksi) = kilo(kg) / boy(m)^2
     */
    public static function calculateBmi(float $weightKg, float $heightCm): float
    {
        $heightM = $heightCm / 100;

        return round($weightKg / ($heightM * $heightM), 1);
    }

    /**
     * Boya göre sağlıklı kilo aralığı (min-max) ve önerilen hedef kilo (aralığın ortası).
     *
     * @return array{min: float, max: float, target: float}
     */
    public static function healthyWeightRange(float $heightCm): array
    {
        $heightM = $heightCm / 100;
        $min = round(self::HEALTHY_BMI_MIN * $heightM * $heightM, 1);
        $max = round(self::HEALTHY_BMI_MAX * $heightM * $heightM, 1);
        $target = round(($min + $max) / 2, 1);

        return ['min' => $min, 'max' => $max, 'target' => $target];
    }

    public static function bmiCategory(float $bmi): string
    {
        return match (true) {
            $bmi < self::HEALTHY_BMI_MIN => 'zayif',
            $bmi <= self::HEALTHY_BMI_MAX => 'normal',
            $bmi < 30 => 'fazla_kilolu',
            default => 'obez',
        };
    }

    /**
     * Doğum tarihinden anlık (tam yıl) yaş hesabı.
     */
    public static function calculateAge(?string $birthDate): ?int
    {
        if (! $birthDate) {
            return null;
        }

        return Carbon::parse($birthDate)->age;
    }
}
