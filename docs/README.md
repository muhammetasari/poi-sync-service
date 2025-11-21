# 🚀 POI Sync Service

POI (Point of Interest) senkronizasyon servisi, Google Places API ile entegre çalışan, Spring Boot & Kotlin tabanlı modern bir backend uygulamasıdır. Hem reaktif hem klasik web servis akışlarını güvenli, ölçeklenebilir ve çoklu dil destekli olarak sunar.

---

## 📦 Kullanılan Teknolojiler

- **Kotlin 1.9.25** & **Java 17** – Modern, tip güvenli programlama ve JVM desteği
- **Spring Boot 3.5.7** – Gelişmiş backend çatısı
- **Spring Web / WebFlux** – Hem klasik hem reaktif REST API’ler
- **Spring Security & JWT** – Kimlik doğrulama, endpoint koruması
- **MongoDB (Atlas)** & **Redis (Upstash Cloud)** – NoSQL ve caching
- **Google Places API, Firebase Admin SDK** – Harici sistem entegrasyonu
- **Gradle 8.5** – Build management ve bağımlılık yönetimi
- **Docker & Docker Compose** – Kolay deploy & local setup
- **Test Stack:** JUnit 5, MockK, Testcontainers, WireMock, Spring Security Test

---

## 🏗️ Başlıca Özellikler

- ✅ RESTful ve reaktif endpoint’ler
- ✅ JWT tabanlı authentication
- ✅ Redis ile cache (24s TTL, SSL’li bağlantı)
- ✅ MongoDB ile veri saklama
- ✅ Google & Firebase entegrasyonları
- ✅ API key ile erişim yetkilendirme
- ✅ Kapsamlı hata ve validasyon yönetimi (i18n)
- ✅ Çoklu dil: Türkçe, İngilizce ve kolay eklenebilir diller
- ✅ Health/metrics endpoint’leri (Spring Actuator)
- ✅ Dokümantasyon dosyaları ve hazır Postman koleksiyonu

---

## 🛠️ Kurulum & Çalıştırma

### Gereksinimler
- Java 17+
- Docker (optional)
- MongoDB Atlas & Upstash Redis hesapları
- Google API Key

### Environment Variables (örn. `.env`)
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

## 📡 Ana API Endpoints

| Endpoint                       | HTTP  | Açıklama                          |
|--------------------------------|-------|-----------------------------------|
| /api/auth/register             | POST  | Kullanıcı kaydı                   |
| /api/auth/login                | POST  | Kullanıcı girişi                  |
| /api/auth/social-login         | POST  | Sosyal login                      |
| /api/sync/locations            | POST  | POI senkronizasyonu başlat        |
| /api/places/nearby             | GET   | Yakındaki POI arama               |
| /api/places/text-search        | GET   | Metin üzerinden POI arama         |
| /api/places/details/{placeId}  | GET   | POI detay sorgusu                 |
| /actuator/health               | GET   | Healthcheck endpoint’i            |

Daha fazlası için Postman koleksiyonunu veya docs/ dizinini inceleyin.

---

## 🏗️ Proje Dizini (Özet)

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

## 🔒 Güvenlik & Cache

- **JWT token** ile authentication
- **API Key** ile request bazlı yetkilendirme (`X-API-Key`)
- **Redis (Upstash, SSL)** ile 24 saatlik cache mekanizması

---

## 🧪 Test

Tüm unit & entegrasyon testlerini çalıştırmak için:
```bash
./gradlew test
```
Test coverage ve örnekler için:
- `/src/test`
- Testcontainers / MockK entegrasyon örnekleri

---

## 🌍 Çoklu Dil Desteği

- `Accept-Language` header ile **Türkçe** veya **İngilizce** response alabilirsin.
- Yeni dil ekleme rehberi için [i18n_GUIDE.md](./i18n_GUIDE.md)
- Error ve validasyon mesajlarında otomatik lokalizasyon aktif.

---

## 🚦 Hata Yönetimi & Response Formatı

- Standart response:
    - `success` (true/false)
    - `data`
    - `code`, `message`, `errors`, `timestamp`
- Hatalar için ayrıntılı kodlar:
    - Kullanıcı, authentication, validasyon, external servis, cache, veritabanı ve sistem hataları ayrıştırılır.
    - Tüm hata kodları için [ERROR_CODES.md](./ERROR_CODES.md)
    - Response örnekleri: [API_RESPONSES.md](./API_RESPONSES.md)

---

## 📝 Diğer Dokümantasyon Dosyaları

- [ERROR_CODES.md](./ERROR_CODES.md) — Hata kodları ve açıklamaları
- [API_RESPONSES.md](./API_RESPONSES.md) — Yanıt formatı örnekleri
- [i18n_GUIDE.md](./i18n_GUIDE.md) — Yeni dil ekleme, i18n yapısı
- Postman koleksiyonu: `docs/POI-Sync-Service.postman_collection.json`

---

## 👥 Katkı & İletişim

Pull request gönderebilir, hata veya öneri iletebilirsin.
Katkı rehberi yakında eklenecek.

---

## 📧 Lisans & İletişim

[Lisans veya iletişim bilgilerini buraya ekle]

---