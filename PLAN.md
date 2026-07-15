# Kilo Takip Uygulaması - Proje Planı

## 1. Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| Mobil | Kotlin, Jetpack Compose, MVVM |
| Local DB | Room (SQLite) - offline-first |
| Network | Retrofit + OkHttp, Kotlin Coroutines/Flow |
| Bildirim | AlarmManager/WorkManager (local) + FCM (cloud senkron) |
| Backend | PHP (Laravel) - REST API |
| DB | MySQL |
| Sunucu | VPS (Nginx + PHP-FPM), SSL (Let's Encrypt) |
| Auth | Laravel Sanctum (kalıcı token, cihaz bazlı) |

## 2. Mimari Prensipler

- **Offline-first**: Kilo kayıtları ve hatırlatıcı ayarları önce Room'a yazılır, sonra arka planda (WorkManager) sunucuya senkronize edilir. İnternet yoksa kuyruğa alınır (`sync_status: pending/synced/failed`).
- **Kalıcı oturum**: Giriş sonrası token cihazda güvenli saklanır (EncryptedSharedPreferences), süresi dolmaz; sadece admin veya kullanıcı manuel çıkış yaparsa sonlanır.
- **Admin oturum sonlandırma**: Backend'de `sessions`/`personal_access_tokens` tablosunda token iptali, mobil tarafta bir sonraki istekte 401 alınca zorla çıkış.
- **IP kara liste**: Login/istek middleware'inde IP kontrolü, admin panelinden IP ban ekleme/kaldırma.

## 3. Veritabanı Şeması (MySQL)

- `users`: id, name, username(unique), phone(unique), email(opsiyonel), password_hash, birth_date, height_cm, current_weight_kg, target_weight_kg(hesaplanan sağlıklı kilo), role(user/admin), status(active/banned), created_at, last_login_at, last_login_ip — ✅ backend (migration/model/AuthController) ve mobil (Login/Register ekranları) uygulandı.
- `login_logs`: id, user_id, ip_address, device_info, created_at
- `weight_entries`: id, user_id, weight, note, recorded_at, sync_status, created_at
- `reminders`: id, user_id, type(su/ilac/hareket/custom), title, days_of_week, time, interval_minutes(opsiyonel tekrar), start_time, end_time, is_active
- `reminder_logs`: id, reminder_id, scheduled_at, status(confirmed/missed/snoozed), responded_at
- `blacklisted_ips`: id, ip_address, reason, created_by(admin_id), created_at
- `devices` (push token eşleme): id, user_id, fcm_token, platform

## 4. Bildirim/Hatırlatıcı Mantığı

1. Kullanıcı hatırlatıcı oluşturur (gün, saat, opsiyonel tekrar aralığı - örn. "09:00-21:00 arası 60 dk'da bir su iç").
2. Ayarlar hem Room'a hem (bağlantı varsa) sunucuya kaydedilir; sunucu FCM ile diğer cihazlara/senkron amaçlı yedek olarak tutar.
3. Cihazda AlarmManager/WorkManager tetikler → bildirim gösterilir → kullanıcı "Onayla/Yaptım" veya yoksay.
4. Onaylanmazsa `reminder_logs.status = missed` olarak kaydedilir (yapılmamış sayılır), onaylanırsa `confirmed`.
5. Geçmiş ekranında hatırlatıcı uyum yüzdesi ve kilo grafiği gösterilir.

## 5. Ekranlar (UI/UX)

- **Onboarding/Login/Register**: Motive edici karşılama.
  - **Login**: Kullanıcı adı/telefon numarası + şifre ile giriş.
  - **Register**: Ad, telefon (veya kullanıcı adı), şifre, doğum tarihi, boy (cm), mevcut kilo (kg).
    - Kayıtta BMI = kilo / (boy_m)² hesaplanır; sonuca göre sağlıklı kilo aralığı (BMI 18.5-24.9 referansı ile min-max kg) ve önerilen hedef kilo kullanıcıya gösterilir ve `target_weight` olarak kaydedilir.
    - Yaş, kayıtta saklanmaz; her girişte doğum tarihinden anlık hesaplanır (yaşa göre öneri/uyarı mesajları için).
- **Ana Sayfa (Dashboard)**: Bugünkü kilo girişi (büyük, animasyonlu buton), güncel trend grafiği, motive edici günlük mesaj, aktif hatırlatıcı özet kartları.
- **Kilo Ekleme**: Hızlı giriş, animasyonlu onay (confetti/check animasyonu), not ekleme.
- **Geçmiş/İstatistik**: Grafik (haftalık/aylık/yıllık), liste görünümü, filtreleme.
- **Hatırlatıcılar**: Liste + oluşturma/düzenleme (gün, saat, tekrar aralığı seçimi).
- **Bildirim Onay Ekranı**: Push geldiğinde "Yaptım / Ertele / Geç" aksiyonları.
- **Profil/Ayarlar**: Hedefler, bildirim tercihleri, çıkış.
- **Admin Paneli (aynı app içinde, rol=admin ise görünür sekme)**: Kullanıcı listesi (kayıt tarihi, son giriş IP, durum), kullanıcı detay/geçmişi, oturum sonlandırma, IP ban listesi yönetimi.

### Tasarım Dili
- Material 3 (Jetpack Compose), yumuşak gradyanlar, canlı motive edici renk paleti (yeşil/turuncu vurgular).
- Mikro-animasyonlar: Lottie (başarı, hedefe ulaşma), sayı sayacı animasyonu (kilo değişimi), ilerleme çubukları.
- Karanlık/Aydınlık tema desteği.

## 6. Güvenlik

- Şifreler bcrypt/argon2 ile hashlenir.
- API istekleri HTTPS üzerinden, Sanctum token ile.
- Rate limiting (login brute-force koruması) + IP kara liste middleware.
- Giriş loglarında IP, cihaz, tarih tutulur; admin bunları görebilir.

## 7. Fazlar

**Faz 0 - Kurulum (1-2 gün)**
- VPS kurulumu (Nginx, PHP, MySQL, SSL), Laravel proje iskeleti, Android proje iskeleti (Compose, paket yapısı).

**Faz 1 - Kimlik Doğrulama & Kullanıcı Yönetimi (3-4 gün)**
- Backend: register (ad, telefon, şifre, doğum tarihi, boy, kilo) / login (kullanıcı adı veya telefon + şifre) / logout API, BMI + sağlıklı kilo hesaplama servisi, doğum tarihinden yaş hesaplama, Sanctum token, login_logs, IP kara liste middleware.
- Mobil: Login (kullanıcı adı/telefon) ve Register (boy/kilo/doğum tarihi dahil) ekranları, BMI sonuç/sağlıklı kilo gösterim ekranı, token saklama, oturum yönetimi.

**Faz 2 - Kilo Takibi (Çekirdek Özellik) (4-5 gün)**
- Room şeması, kilo ekleme/listeleme/silme (local + sync).
- Backend: weight_entries CRUD API.
- Dashboard ve geçmiş/grafik ekranları.

**Faz 3 - Hatırlatıcı Sistemi (5-6 gün)**
- Backend: reminders/reminder_logs CRUD API.
- Mobil: Hatırlatıcı oluşturma UI, AlarmManager/WorkManager entegrasyonu, bildirim onay akışı, offline kuyruk senkronizasyonu.
- FCM entegrasyonu (opsiyonel yedek/çoklu cihaz senkron).

**Faz 4 - Admin Paneli (3-4 gün)**
- Backend: admin-only endpoint'ler (kullanıcı listesi, oturum sonlandırma, IP ban).
- Mobil: Rol bazlı admin sekmesi/ekranları.

**Faz 5 - UI/UX Cilalama & Animasyonlar (3-4 gün)**
- Lottie animasyonları, geçiş animasyonları, motive edici mikro-metinler/bildirimler, tema desteği.

**Faz 6 - Test & Yayın (3-4 gün)**
- Uçtan uca test, hata düzeltme, Play Store hazırlığı (ikon, ekran görüntüleri, gizlilik politikası).

## 8. Sonraki Adım

Onaylarsan Faz 0'dan başlayıp VPS/backend iskeletini ve Android proje yapısını kuralım.
