# POI Sync Service

POI (Point of Interest) senkronizasyon servisi, Google Places API ile entegre çalışan, Spring Boot & Kotlin tabanlı modern bir backend uygulamasıdır. Hem reaktif hem klasik web servis akışlarını güvenli, ölçeklenebilir ve çoklu dil destekli olarak sunar.

---

## Kullanılan Teknolojiler

- Kotlin 1.9.25 & Java 17
- Spring Boot 3.5.7
- Spring Web / WebFlux
- Spring Security & JWT (0.12.5)
- MongoDB (Atlas) & Redis (Upstash Cloud)
- Google Places API, Firebase Admin SDK 9.2.0
- SpringDoc OpenAPI 2.8.14
- Gradle 8.5
- Docker & Docker Compose
- Test Stack: JUnit 5, MockK 1.13.8, Testcontainers 1.19.3, WireMock 3.3.1, Spring Security Test

---

## Temel Özellikler

- RESTful ve reaktif endpoint'ler
- JWT tabanlı authentication
- Redis ile cache (24 saat TTL, SSL'li bağlantı)
- MongoDB ile veri saklama
- Google & Firebase entegrasyonları
- API key ile erişim yetkilendirme
- Kapsamlı hata ve validasyon yönetimi (i18n)
- Çoklu dil desteği
- Swagger/OpenAPI UI ile interaktif API dokümantasyonu
- Health/metrics endpoint'leri (Spring Actuator)
- Asenkron POI senkronizasyonu (Job-based)
- Dokümantasyon dosyaları ve hazır Postman koleksiyonu

---

## Kurulum & Çalıştırma

### Gereksinimler
- Java 17+
- Docker (opsiyonel)
- MongoDB Atlas & Upstash Redis hesapları
- Google API Key

### Environment Variables (ör. `.env`)
```ini
SPRING_DATA_REDIS_HOST=...
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=...
SPRING_DATA_REDIS_SSL=true
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/database
GOOGLE_API_KEY=...
API_SECRET_KEY=...
JWT_SECRET=...
SPRING_PROFILES_ACTIVE=docker
LOGGING_LEVEL_COM_ROVITS=INFO
```

### Klonlama & Çalıştırma

```bash
git clone <repository-url>
cd poi-sync-service

# Build için
./gradlew build

# Lokal başlat (Docker olmadan)
./gradlew bootRun

# Ya da Docker Compose ile
docker-compose up --build
```

> `.env` dosyasını doldurmayı unutma! (Bkz: `.env.example`)

---

## Ana API Endpoints

| Endpoint                       | HTTP  | Açıklama                          |
|--------------------------------|-------|-----------------------------------|
| /api/auth/register             | POST  | Kullanıcı kaydı (Firebase token)  |
| /api/auth/login                | POST  | Kullanıcı girişi (Firebase token) |
| /api/auth/password-reset       | POST  | Şifre sıfırlama email gönderimi   |
| /api/auth/refresh              | POST  | Token yenileme                    |
| /api/auth/logout               | POST  | Kullanıcı çıkışı (token blacklist)|
| /api/sync/locations            | POST  | POI senkronizasyonu başlat (Async)|
| /api/sync/status/{jobId}       | GET   | Sync job durumu sorgula           |
| /api/places/nearby             | GET   | Yakındaki POI arama               |
| /api/places/text-search        | GET   | Metin üzerinden POI arama         |
| /api/places/details/{placeId}  | GET   | POI detay sorgusu                 |
| /actuator/health               | GET   | Healthcheck endpoint'i            |

Daha fazlası için Postman koleksiyonunu, **Swagger UI** (`/swagger-ui/index.html`) veya [API dokümantasyonunu](./api/ENDPOINTS.md) inceleyin.

---

## Proje Dizini (Özet)

```
src/main/kotlin/com/rovits/poisyncservice/
├── client/          # Harici API istemcileri
├── config/          # Konfigürasyon dosyaları
├── controller/      # REST Controller’lar
├── domain/          # Model / DTO’lar
├── repository/      # Veritabanı repository’leri
├── service/         # İş mantığı
└── PoiSyncServiceApplication.kt
```

---

## Güvenlik & Cache

- JWT token ile authentication
- API Key ile request bazlı yetkilendirme (`X-API-Key`)
- Redis (Upstash, SSL) ile 24 saatlik cache mekanizması

---

## Test

Tüm unit & entegrasyon testlerini çalıştırmak için:
```bash
./gradlew test
```
Test coverage ve örnekler için:
- `/src/test`
- Testcontainers / MockK entegrasyon örnekleri

---

## Çoklu Dil Desteği

- `Accept-Language` header ile **Türkçe** veya **İngilizce** response alabilirsin.
- Yeni dil ekleme rehberi için [i18n_GUIDE.md](./i18n/i18n_GUIDE.md)
- Error ve validasyon mesajlarında otomatik lokalizasyon aktif.

---

## Hata Yönetimi & Response Formatı

- Standart response:
    - `success` (true/false)
    - `data`
    - `code`, `message`, `errors`, `timestamp`
- Hatalar için ayrıntılı kodlar:
    - Kullanıcı, authentication, validasyon, external servis, cache, veritabanı ve sistem hataları ayrıştırılır.
    - Tüm hata kodları için [ERROR_CODES.md](./errors/ERROR_CODES.md)
    - Response örnekleri: [API_RESPONSES.md](./api/API_RESPONSES.md)

---

## Dokümantasyon

### API & Endpoint Dokümantasyonu
- [ENDPOINTS.md](./api/ENDPOINTS.md) — Detaylı endpoint referansı ve örnekleri
- [ERROR_CODES.md](./errors/ERROR_CODES.md) — Hata kodları ve açıklamaları
- [API_RESPONSES.md](./api/API_RESPONSES.md) — Yanıt formatı örnekleri
- [CLIENT_INTEGRATION.md](./api/CLIENT_INTEGRATION.md) — Client-side (iOS/Android/Web) entegrasyon

### Authentication & Firebase
- [WHY_FIREBASE_AUTH.md](./auth/WHY_FIREBASE_AUTH.md) — Firebase Authentication neden tercih edildi?
- [FIREBASE_SETUP.md](./firebase/FIREBASE_SETUP.md) — Firebase Console kurulum rehberi
- [AUTH_MIGRATION_SUMMARY.md](./auth/AUTH_MIGRATION_SUMMARY.md) — Auth sistem migration detayları

### Test & Development
- [POSTMAN_COLLECTION_GUIDE.md](./postman/POSTMAN_COLLECTION_GUIDE.md) — Postman collection kullanım rehberi
- [POSTMAN_UPDATE_SUMMARY.md](./postman/POSTMAN_UPDATE_SUMMARY.md) — Postman collection güncelleme detayları
- [i18n_GUIDE.md](./i18n/i18n_GUIDE.md) — Yeni dil ekleme, i18n yapısı
- [CHANGELOG.md](./changelog/CHANGELOG.md) — Versiyon değişiklikleri ve geçmiş
- [IMPLEMENTATION_COMPLETE.md](./general/IMPLEMENTATION_COMPLETE.md) — Proje tamamlanma durumu

### Postman Koleksiyonu
- `postman_collection.json` (proje kök dizininde)

---

## Katkı & İletişim

Pull request gönderebilir, hata veya öneri iletebilirsin.
Katkı rehberi yakında eklenecek.

---

## Lisans & İletişim

[Lisans veya iletişim bilgilerini buraya ekle]

---

