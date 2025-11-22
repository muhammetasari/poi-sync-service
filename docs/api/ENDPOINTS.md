# ENDPOINTS.md

Bu dokümanda POI Sync Service'in tüm API endpoint'leri detaylı şekilde açıklanmıştır.

---

## Base URL

```
Local: http://localhost:8080
Production: [Your Production URL]
```

---

## Authentication & Authorization

Çoğu endpoint için aşağıdaki header'lardan biri veya her ikisi gereklidir:

| Header            | Değer                | Açıklama                        |
|-------------------|----------------------|---------------------------------|
| `X-API-Key`       | `{API_SECRET_KEY}`   | Endpoint erişimi için gerekli   |
| `Authorization`   | `Bearer {JWT_TOKEN}` | Kullanıcı kimlik doğrulama      |
| `Accept-Language` | `tr` veya `en`       | Yanıt dilini belirler (opsiyonel)|

---

## Endpoint Kategorileri

1. Authentication API - Kullanıcı kayıt, giriş ve çıkış
2. User API - Kullanıcı işlemleri
3. Place API - POI işlemleri
4. Sync API - Senkronizasyon işlemleri

Her kategori altında ilgili endpoint'ler ve örnek istekler detaylandırılmalıdır.

---

## 1. Authentication API

### 1.1 Kullanıcı Kaydı

**Endpoint:** `POST /api/auth/register`

**Açıklama:** Yeni kullanıcı hesabı oluşturur. Client önce Firebase'de kullanıcı oluşturmalı, ardından Firebase ID token'ı backend'e göndermelidir.

**Headers:**
- `X-API-Key`: Gerekli
- `Content-Type`: `application/json`

**Request Body:**
```json
{
  "idToken": "firebase-id-token-here..."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "507f1f77bcf86cd799439011",
      "name": "Ali Veli",
      "email": "ali@example.com"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh-token-here..."
  },
  "timestamp": "2025-11-22T10:30:00"
}
```

**Error Responses:**
- `400` - Validasyon hatası veya geçersiz Firebase token (VAL_001, AUTH_005)
- `409` - Kullanıcı zaten kayıtlı (USER_002)

---

### 1.2 Kullanıcı Girişi

**Endpoint:** `POST /api/auth/login`

**Açıklama:** Firebase ID token ile kullanıcı girişi yapar. Email/password ve sosyal login (Google, Facebook, Apple) için çalışır. Client önce Firebase ile kimlik doğrulama yapmalı, ardından ID token'ı göndermelidir.

**Headers:**
- `X-API-Key`: Gerekli
- `Content-Type`: `application/json`

**Request Body:**
```json
{
  "idToken": "firebase-id-token-here..."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "507f1f77bcf86cd799439011",
      "name": "Ali Veli",
      "email": "ali@example.com"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh-token-here..."
  },
  "timestamp": "2025-11-22T10:35:00"
}
```

**Error Responses:**
- `400` - Validasyon hatası (VAL_001)
- `401` - Geçersiz Firebase token veya email doğrulanmamış (AUTH_005, AUTH_009)

---

### 1.3 Şifre Sıfırlama

**Endpoint:** `POST /api/auth/password-reset`

**Açıklama:** Kullanıcıya şifre sıfırlama email'i gönderir (Firebase üzerinden).

**Headers:**
- `X-API-Key`: Gerekli
- `Content-Type`: `application/json`

**Request Body:**
```json
{
  "email": "ali@example.com"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": "Password reset email sent successfully",
  "timestamp": "2025-11-22T10:40:00"
}
```

**Error Responses:**
- `400` - Validasyon hatası (VAL_002)
- `404` - Kullanıcı bulunamadı (USER_001)

---

### 1.4 Token Yenileme

**Endpoint:** `POST /api/auth/refresh`

**Açıklama:** Refresh token kullanarak yeni access token alır.

**Headers:**
- `X-API-Key`: Gerekli
- `Content-Type`: `application/json`

**Request Body:**
```json
{
  "refreshToken": "refresh-token-here..."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "token": "new-access-token...",
    "refreshToken": "new-refresh-token..."
  },
  "timestamp": "2025-11-22T10:42:00"
}
```

**Error Responses:**
- `401` - Token geçersiz veya süresi dolmuş (AUTH_001, AUTH_002)

---

### 1.5 Kullanıcı Çıkışı (Logout)

**Endpoint:** `POST /api/auth/logout`

**Açıklama:** Mevcut access token'ı ve isteğe bağlı olarak refresh token'ı blacklist'e ekler.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli
- `Content-Type`: `application/json`

**Request Body (Optional):**
```json
{
  "refreshToken": "refresh-token-to-invalidate..."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": null,
  "timestamp": "2025-11-22T10:45:00"
}
```

**Error Responses:**
- `401` - Token geçersiz veya eksik (AUTH_002, AUTH_007)

---

## 2. User API

### 2.1 Kullanıcı Bilgilerini Getir

**Endpoint:** `GET /api/user/profile`

**Açıklama:** Mevcut kullanıcının profil bilgilerini döndürür.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "name": "Ali Veli",
    "email": "ali@example.com",
    "createdAt": "2025-11-22T10:30:00",
    "updatedAt": "2025-11-22T10:30:00"
  },
  "timestamp": "2025-11-22T10:50:00"
}
```

**Error Responses:**
- `401` - Yetkisiz erişim (AUTH_003)

---

### 2.2 Kullanıcı Bilgilerini Güncelle

**Endpoint:** `PUT /api/user/profile`

**Açıklama:** Mevcut kullanıcının profil bilgilerini günceller.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli
- `Content-Type`: `application/json`

**Request Body:**
```json
{
  "name": "Yeni İsim",
  "email": "yeniemail@example.com"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "name": "Yeni İsim",
    "email": "yeniemail@example.com",
    "createdAt": "2025-11-22T10:30:00",
    "updatedAt": "2025-11-22T10:55:00"
  },
  "timestamp": "2025-11-22T10:55:00"
}
```

**Error Responses:**
- `400` - Validasyon hatası (VAL_003)
- `401` - Yetkisiz erişim (AUTH_003)

---

## 3. Place API

### 3.1 Yakın POI'ları Getir

**Endpoint:** `GET /api/places/nearby`

**Açıklama:** Belirtilen koordinat etrafındaki POI'ları arar. Hibrit arama stratejisi kullanır (Redis → MongoDB → Google API).

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli

**Query Parameters:**

| Parametre | Tip     | Gerekli | Varsayılan | Açıklama                          |
|-----------|---------|---------|-----------|-----------------------------------|
| `lat`     | Double  | Evet    | -         | Enlem (-90 ile 90 arası)         |
| `lng`     | Double  | Evet    | -         | Boylam (-180 ile 180 arası)     |
| `radius`  | Double  | Hayır   | 1000.0    | Arama yarıçapı (metre)          |
| `type`    | String  | Hayır   | restaurant | POI tipi (restaurant, cafe, gym) |

**Örnek İstek:**
```
GET /api/places/nearby?lat=41.0082&lng=28.9784&radius=2000&type=cafe
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "places": [
      {
        "id": "ChIJN1t_tDeuEmsRUsoyG83frY4",
        "name": "Starbucks İstiklal",
        "address": "İstiklal Caddesi No:123, Beyoğlu",
        "location": {
          "latitude": 41.0082,
          "longitude": 28.9784
        },
        "rating": 4.5,
        "types": ["cafe", "food"]
      }
    ],
    "totalResults": 15
  },
  "timestamp": "2025-11-22T11:00:00"
}
```

**Error Responses:**
- `400` - Geçersiz koordinatlar veya yarıçap (VAL_005, VAL_006, VAL_007)
- `401` - Yetkisiz erişim (AUTH_003)
- `429` - Rate limit aşıldı (AUTH_008)

---

### 3.2 Metin ile POI Ara

**Endpoint:** `GET /api/places/text-search`

**Açıklama:** Metin sorgusu ile POI arar. İsteğe bağlı konum bias'ı destekler.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli

**Query Parameters:**

| Parametre     | Tip      | Gerekli | Varsayılan | Açıklama                          |
|---------------|----------|---------|-----------|-----------------------------------|
| `query`       | String   | Evet    | -         | Arama sorgusu                    |
| `languageCode`| String   | Hayır   | tr        | Sonuç dili (tr, en)              |
| `maxResults`  | Integer  | Hayır   | 10        | Maksimum sonuç sayısı            |
| `lat`         | Double   | Hayır   | -         | Konum bias için enlem            |
| `lng`         | Double   | Hayır   | -         | Konum bias için boylam           |
| `radius`      | Double   | Hayır   | -         | Konum bias için yarıçap          |

**Örnek İstek:**
```
GET /api/places/text-search?query=Best%20sushi%20in%20Istanbul&maxResults=5&languageCode=en
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "places": [
      {
        "id": "ChIJ...",
        "name": "Tokyo Dining",
        "address": "Nişantaşı, Istanbul",
        "location": {
          "latitude": 41.0082,
          "longitude": 28.9784
        },
        "rating": 4.8,
        "types": ["restaurant", "japanese"]
      }
    ],
    "totalResults": 5
  },
  "timestamp": "2025-11-22T11:10:00"
}
```

**Error Responses:**
- `400` - Validasyon hatası (VAL_004)
- `401` - Yetkisiz erişim (AUTH_003)

---

### 3.3 POI Detaylarını Getir

**Endpoint:** `GET /api/places/details/{placeId}`

**Açıklama:** Belirli bir POI'nin detaylı bilgilerini döndürür.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli

**Path Parameters:**

| Parametre | Tip   | Açıklama                                      |
|-----------|-------|-----------------------------------------------|
| `placeId` | String| Google Places API'den alınan benzersiz POI ID'si |

**Örnek İstek:**
```
GET /api/places/details/ChIJN1t_tDeuEmsRUsoyG83frY4
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "ChIJN1t_tDeuEmsRUsoyG83frY4",
    "name": "Starbucks İstiklal",
    "address": "İstiklal Caddesi No:123, Beyoğlu, İstanbul",
    "location": {
      "latitude": 41.0082,
      "longitude": 28.9784
    },
    "rating": 4.5,
    "userRatingsTotal": 1234,
    "types": ["cafe", "food", "store"],
    "phoneNumber": "+90 212 123 4567",
    "website": "https://www.starbucks.com.tr",
    "openingHours": {
      "weekdayText": [
        "Pazartesi: 07:00 - 23:00",
        "Salı: 07:00 - 23:00",
        "..."
      ],
      "openNow": true
    }
  },
  "timestamp": "2025-11-22T11:15:00"
}
```

**Error Responses:**
- `404` - POI bulunamadı (POI_001)
- `401` - Yetkisiz erişim (AUTH_003)

---

## 4. Sync API

### 4.1 POI Senkronizasyonu Başlat

**Endpoint:** `POST /api/sync/locations`

**Açıklama:** Belirtilen koordinat ve yarıçap içindeki POI'ları Google API'den çekip MongoDB'ye kaydeder. İşlem asenkron olarak arka planda çalışır.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli (Admin rolü)

**Query Parameters:**

| Parametre | Tip     | Gerekli | Varsayılan | Açıklama                          |
|-----------|---------|---------|-----------|-----------------------------------|
| `lat`     | Double  | Evet    | -         | Enlem                             |
| `lng`     | Double  | Evet    | -         | Boylam                            |
| `radius`  | Double  | Hayır   | 1000.0    | Yarıçap (metre)                  |
| `type`    | String  | Hayır   | restaurant | POI tipi                         |

**Örnek İstek:**
```
POST /api/sync/locations?lat=41.0082&lng=28.9784&radius=5000&type=restaurant
```

**Success Response (202 Accepted):**
```json
{
  "success": true,
  "data": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-11-22T11:20:00"
}
```
> `data` alanı job ID'sini içerir.

**Error Responses:**
- `400` - Validasyon hatası (VAL_005, VAL_006, VAL_007)
- `401` - Yetkisiz erişim (AUTH_003)
- `403` - Admin rolü gerekli (AUTH_004)

---

### 4.2 Senkronizasyon Durumu Sorgula

**Endpoint:** `GET /api/sync/status/{jobId}`

**Açıklama:** Belirtilen job ID'sine sahip senkronizasyon işinin durumunu döndürür.

**Headers:**
- `X-API-Key`: Gerekli
- `Authorization`: `Bearer {JWT_TOKEN}` - Gerekli

**Path Parameters:**

| Parametre | Tip   | Açıklama                                      |
|-----------|-------|-----------------------------------------------|
| `jobId`   | String| Senkronizasyon başlatıldığında dönen job ID |

**Örnek İstek:**
```
GET /api/sync/status/550e8400-e29b-41d4-a716-446655440000
```

**Success Response - COMPLETED (200):**
```json
{
  "success": true,
  "data": {
    "status": "COMPLETED"
  },
  "timestamp": "2025-11-22T11:25:00"
}
```

**Success Response - IN_PROGRESS (200):**
```json
{
  "success": true,
  "data": {
    "status": "IN_PROGRESS"
  },
  "timestamp": "2025-11-22T11:22:00"
}
```

**Success Response - FAILED (200):**
```json
{
  "success": true,
  "data": {
    "status": "FAILED",
    "error": "Google API rate limit exceeded"
  },
  "timestamp": "2025-11-22T11:23:00"
}
```

**Error Responses:**
- `404` - Job bulunamadı (POI_001)
- `401` - Yetkisiz erişim (AUTH_003)

---

## 5. Health Check

### 5.1 Servis Sağlık Kontrolü

**Endpoint:** `GET /actuator/health`

**Açıklama:** Servisin ve bağımlılıklarının sağlık durumunu kontrol eder.

**Headers:** Gerekli değil (public endpoint)

**Success Response (200):**
```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

**Error Response (503):**
```json
{
  "status": "DOWN",
  "components": {
    "mongo": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    }
  }
}
```

---

## Swagger/OpenAPI Dokümantasyonu

Tüm endpoint'lerin interaktif dokümantasyonu için Swagger UI'ı ziyaret edebilirsiniz:

```
http://localhost:8080/swagger-ui.html
```

Swagger UI üzerinden:
- Tüm endpoint'leri görebilir
- Request/response şemalarını inceleyebilir
- Doğrudan API çağrıları test edebilirsiniz

---

## İlgili Dökümanlar

- [ERROR_CODES.md](./ERROR_CODES.md) - Tüm hata kodları
- [API_RESPONSES.md](./API_RESPONSES.md) - Response format örnekleri
- [i18n_GUIDE.md](./i18n_GUIDE.md) - Çoklu dil desteği
- [README.md](./README.md) - Genel proje bilgileri

---

## Notlar

1. **Rate Limiting:** Bazı endpoint'ler rate limit'e tabidir. Çok fazla istek gönderirseniz `AUTH_008` hatası alabilirsiniz.

2. **Caching:** Places API sonuçları Redis'te 24 saat cache'lenir. Güncel veriye ihtiyacınız varsa sync endpoint'ini kullanın.

3. **Job Tracking:** Senkronizasyon işleri asenkrondur. Job ID'yi saklayıp düzenli olarak durumu kontrol edin.

4. **Authentication:** Çoğu endpoint hem API Key hem de JWT token gerektirir. Logout hariç tüm endpoint'ler için her iki header'ı da göndermeniz önerilir.

---
