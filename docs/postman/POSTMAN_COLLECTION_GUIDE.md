# POSTMAN_COLLECTION_GUIDE.md

Bu rehber, POI Sync Service API'sinin güncellenmiş Postman collection'ını nasıl kullanacağınızı açıklar.

**Son Güncelleme:** 22 Kasım 2025

---

## Collection'ı İçe Aktarma

1. Postman uygulamasını açın
2. **Import** butonuna tıklayın
3. `postman_collection.json` dosyasını seçin veya sürükleyip bırakın
4. Collection import edildikten sonra sol panelde görünecektir

---

## Environment Değişkenlerini Yapılandırma

Collection içinde 5 adet değişken tanımlanmıştır:

| Değişken | Açıklama | Varsayılan Değer | Nasıl Ayarlanır |
|----------|----------|------------------|-----------------|
| `baseUrl` | API base URL'i | `http://localhost:8080` | Manuel olarak ayarlayın |
| `apiKey` | X-API-Key header için gerekli | `your_api_secret_key_here` | Manuel olarak ayarlayın |
| `token` | JWT access token | (boş) | Login/Register sonrası otomatik dolar |
| `refreshToken` | JWT refresh token | (boş) | Login/Register sonrası otomatik dolar |
| `jobId` | Sync job ID | (boş) | Sync start sonrası otomatik dolar |

### Değişkenleri Ayarlama

1. Collection'a sağ tıklayın
2. **Edit** seçeneğini tıklayın
3. **Variables** sekmesine gidin
4. `baseUrl` ve `apiKey` değerlerini güncelleyin
5. **Save** butonuna tıklayın

---

## API Test Akışı

### Authentication Test Akışı

#### A. Yeni Kullanıcı Kaydı
```
1. Firebase'de kullanıcı oluşturun (client tarafında)
2. Firebase ID token'ı alın
3. Postman'de "Register" endpoint'ini açın
4. Body'de idToken'ı yapıştırın
5. Send
```

#### B. Mevcut Kullanıcı Girişi
```
1. Firebase'de kullanıcı authenticate edin (client tarafında)
2. Firebase ID token'ı alın
3. Postman'de "Login" endpoint'ini açın
4. Body'de idToken'ı yapıştırın
5. Send
```

#### C. Şifre Sıfırlama
```
1. "Send Password Reset Email" endpoint'ini açın
2. Body'de email adresini girin
3. Send
```

### Places API Test Akışı

Önce authentication yapıldığından emin olun (token dolu olmalı)!

#### A. Yakındaki Yerleri Arama
```
1. "Search Nearby" endpoint'ini açın
2. Query parameters:
   - lat: 41.0082 (İstanbul Taksim)
   - lng: 28.9784
   - radius: 2000 (2 km)
   - type: cafe
3. Send
```

#### B. Metin ile Arama
```
1. "Search Text" endpoint'ini açın
2. Query parameters:
   - query: "Best sushi in Istanbul"
   - languageCode: en
   - maxResults: 5
3. (Opsiyonel) Location bias için lat, lng, radius ekleyin
4. Send
```

#### C. POI Detayları
```
1. Önce Search Nearby veya Search Text ile bir place ID alın
2. "Get Place Details" endpoint'ini açın
3. Path variable'da placeId'yi girin
4. Send
```

### Sync API Test Akışı (Admin Only)

**⚠️ DİKKAT:** Bu endpoint'ler için ADMIN rolüne sahip olmanız gerekir!

#### A. Senkronizasyon Başlatma
```
1. Admin token ile giriş yapın
2. "Start Location Sync" endpoint'ini açın
3. Query parameters:
   - lat: 41.0082
   - lng: 28.9784
   - radius: 5000
   - type: restaurant
4. Send
```

#### B. Senkronizasyon Durumu Kontrolü
```
1. "Get Sync Job Status" endpoint'ini açın
2. Path variable'da jobId zaten otomatik dolu olacak ({{jobId}})
3. Send
```

### Health Check
```
1. "Health Check" endpoint'ini açın
2. Send (authentication gerektirmez)
```

---

## Header'lar

### Tüm Endpoint'ler için Gerekli Header'lar

| Header | Değer | Kullanıldığı Endpoint'ler |
|--------|-------|---------------------------|
| `X-API-Key` | `{{apiKey}}` | Tüm endpoint'ler (Health Check hariç) |
| `Authorization` | `Bearer {{token}}` | Places API, Sync API, Logout, Update Role |
| `Content-Type` | `application/json` | POST/PUT request'leri |
| `Accept-Language` | `tr` veya `en` (opsiyonel) | Tüm endpoint'ler |

---

## Otomatik Test Script'leri

Collection'da bazı endpoint'lere otomatik test script'leri eklenmiştir:

### Register & Login
```javascript
// Response'dan token'ı otomatik çıkarır ve variable'a kaydeder
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data && jsonData.data.token) {
        pm.collectionVariables.set("token", jsonData.data.token);
        pm.collectionVariables.set("refreshToken", jsonData.data.refreshToken);
        console.log("Token saved:", jsonData.data.token);
    }
}
```

### Start Location Sync
```javascript
// Response'dan job ID'yi otomatik çıkarır ve variable'a kaydeder
if (pm.response.code === 202) {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data) {
        pm.collectionVariables.set("jobId", jsonData.data);
        console.log("Job ID saved:", jsonData.data);
    }
}
```

---

## Collection İçeriği

### Toplam 11 Endpoint

#### 1. **Auth** (6 endpoint)
- ✅ Register
- ✅ Login
- ✅ Send Password Reset Email
- ✅ Send Email Verification
- ✅ Update User Role (Admin)
- ✅ Logout

#### 2. **Places** (3 endpoint)
- ✅ Search Nearby
- ✅ Search Text
- ✅ Get Place Details

#### 3. **Sync** (2 endpoint)
- ✅ Start Location Sync
- ✅ Get Sync Job Status

#### 4. **Health** (1 endpoint)
- ✅ Health Check

---

## Farklı Environment'lar için Kullanım

### Local Development
```json
{
  "baseUrl": "http://localhost:8080",
  "apiKey": "dev_api_key_here"
}
```

### Docker
```json
{
  "baseUrl": "http://localhost:8080",
  "apiKey": "docker_api_key_here"
}
```

### Production (Render)
```json
{
  "baseUrl": "https://your-app.onrender.com",
  "apiKey": "production_api_key_here"
}
```

**Öneri:** Her environment için ayrı Postman Environment dosyası oluşturun:
1. Postman'de **Environments** sekmesine gidin
2. **Create Environment** butonuna tıklayın
3. Environment adını verin (Local, Docker, Production)
4. `baseUrl` ve `apiKey` değişkenlerini tanımlayın
5. Environment'ı seçin ve test edin

---

## Sık Karşılaşılan Hatalar ve Çözümleri

### 1. 401 Unauthorized (AUTH_003)
**Sebep:** Token eksik veya geçersiz

**Çözüm:**
- Login veya Register endpoint'ini tekrar çalıştırın
- Token'ın otomatik kaydedildiğinden emin olun
- Authorization header'ın doğru formatta olduğunu kontrol edin: `Bearer {{token}}`

### 2. 401 API Key Missing (AUTH_001)
**Sebep:** X-API-Key header'ı eksik veya yanlış

**Çözüm:**
- Collection variables'da `apiKey` değişkenini kontrol edin
- Header'da `X-API-Key: {{apiKey}}` olduğundan emin olun

### 3. 403 Forbidden (AUTH_004)
**Sebep:** Admin rolü gerekli, ancak normal kullanıcı ile giriş yapılmış

**Çözüm:**
- Admin rolüne sahip bir kullanıcı ile giriş yapın
- Veya "Update User Role" endpoint'i ile rolünüzü güncelleyin (başka bir admin gereklidir)

### 4. 404 Job Not Found (POI_001)
**Sebep:** Geçersiz job ID veya job süresi dolmuş

**Çözüm:**
- Sync işlemini tekrar başlatın
- Yeni job ID'yi kullanın

### 5. 400 Validation Error
**Sebep:** Request body veya query parameters hatalı

**Çözüm:**
- Endpoint açıklamasını okuyun
- Required parametrelerin dolu olduğundan emin olun
- Veri tiplerini kontrol edin (string, number, boolean)

---

## İpuçları

1. **Token Yönetimi:** Login/Register sonrası token'lar otomatik kaydedilir, manuel kopyalama gerektirmez

2. **Job Tracking:** Sync başlattığınızda job ID otomatik kaydedilir, status endpoint'inde doğrudan kullanılır

3. **Language Support:** `Accept-Language` header'ını kullanarak TR veya EN response alabilirsiniz (varsayılan: TR)

4. **Rate Limiting:** Çok fazla istek gönderirseniz 429 hatası alabilirsiniz, birkaç saniye bekleyin

5. **Caching:** Places API sonuçları 24 saat cache'lenir, güncel veri için Sync API'yi kullanın

6. **Path Variables:** Endpoint URL'lerinde `:paramName` şeklinde path variable'lar vardır, bunları değiştirmeyi unutmayın

---

## İlgili Dokümanlar

- [ENDPOINTS.md](./ENDPOINTS.md) - Tüm endpoint'lerin detaylı açıklaması
- [ERROR_CODES.md](./ERROR_CODES.md) - Tüm hata kodları ve anlamları
- [API_RESPONSES.md](./API_RESPONSES.md) - Response format örnekleri
- [FIREBASE_SETUP.md](./FIREBASE_SETUP.md) - Firebase kurulum rehberi
- [CLIENT_INTEGRATION.md](./CLIENT_INTEGRATION.md) - Client entegrasyon rehberi

---

## Destek

Herhangi bir sorun yaşarsanız:
1. İlgili endpoint'in description'ını okuyun
2. Console'da error mesajlarını kontrol edin
3. [ERROR_CODES.md](./ERROR_CODES.md) dokümanına bakın
4. Proje README'sinde belirtilen iletişim kanallarını kullanın

---

**Happy Testing!**
