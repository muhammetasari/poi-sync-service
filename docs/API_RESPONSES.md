# 📦 API_RESPONSES.md

## Genel Response Formatı

Tüm endpoint'ler başarılı ve hatalı durumlarda **standart bir response yapısı** döndürür.  
Başarılı yanıtlar ile hata yanıtları arasında `success` bayrağı ve `data`/`error` alanları kullanılır.

---

### 🟢 Başarılı Response

```json
{
  "success": true,
  "data": {
    /* endpoint'e göre değişen içerik */
  },
  "timestamp": "2025-11-18T13:00:00"
}
```

#### Alanlar

| Alan        | Tipi       | Açıklama                        |
| ----------- | ---------- | ------------------------------- |
| success     | boolean    | İşlemin başarılı olup olmadığı  |
| data        | object     | Sonuç/cevap (endpoint'e göre)   |
| timestamp   | string     | Response zamanı (ISO-8601)      |

---

#### Örnek - Kayıt Başarılı

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "abc123",
      "name": "Ali Veli",
      "email": "ali@ornek.com"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "long-refresh-token..."
  },
  "timestamp": "2025-11-18T13:01:00"
}
```

---

### 🔴 Hata Response (Genel)

```json
{
  "success": false,
  "code": "USER_001",
  "message": "İstek yapılan email adresine sahip kullanıcı bulunamadı",
  "errors": null,
  "timestamp": "2025-11-18T13:00:12"
}
```

#### Alanlar

| Alan      | Tipi        | Açıklama                                   |
| --------- | ----------- | ------------------------------------------ |
| success   | boolean     | Her zaman `false`                          |
| code      | string      | Hata kodu (bkz: ERROR_CODES.md)            |
| message   | string      | i18n ile lokalize edilen hata mesajı       |
| errors    | array/null  | Alan bazlı (field) hata listesi (opsiyonel)|
| timestamp | string      | Hata zamanı (ISO-8601)                     |

---

### 🟠 Validasyon Hatası Response

```json
{
  "success": false,
  "code": "VAL_001",
  "message": "İstek doğrulaması başarısız oldu",
  "errors": [
    {
      "field": "name",
      "message": "İsim 2 ile 100 karakter arasında olmalıdır",
      "rejectedValue": "A"
    },
    {
      "field": "email",
      "message": "Geçersiz e-posta formatı: invalid",
      "rejectedValue": "invalid"
    },
    {
      "field": "password",
      "message": "Şifre en az 8 karakter olmalıdır",
      "rejectedValue": "123"
    }
  ],
  "timestamp": "2025-11-18T13:05:40"
}
```

#### errors[] Detay

| Alan         | Tipi     | Açıklama                        |
| ------------ | -------- | ------------------------------- |
| field        | string   | Hata olan alan (form field)     |
| message      | string   | Alan için lokalize hata mesajı  |
| rejectedValue| any      | Kullanıcıdan gelen değer        |

---

### 🟣 Hata Response (Yetkisiz/Anahtar Eksik)

```json
{
  "success": false,
  "code": "AUTH_003",
  "message": "Bu kaynağa erişim yetkiniz bulunmamaktadır",
  "errors": null,
  "timestamp": "2025-11-18T13:07:49"
}
```

---

### ⬛ Sistem Hatası (Bilinmeyen/500)

```json
{
  "success": false,
  "code": "SYS_001",
  "message": "Sunucu içi hata oluştu",
  "errors": null,
  "timestamp": "2025-11-18T13:08:23"
}
```

---

## 🌐 Dil (i18n) Destekli Yanıtlar

- Hata mesajları ve validasyon düğümleri **Accept-Language** HTTP başlığı ile otomatik lokalize edilir.
- Desteklenen diller: Türkçe (`tr`), İngilizce (`en`)

---

## 📝 Notlar

- **Başarılı yanıtlar** her zaman `success: true`, **hatalı yanıtlar** ise `success: false` ile başlar.
- Detaylı validasyon hatalarında `errors` dizisi; genel sistem/app hatalarında `errors` alanı `null` olur.
- `code` alanları ve anlamları için bkz: [ERROR_CODES.md](./ERROR_CODES.md)
- Gelişmiş/detaylı endpoint örnekleri için Postman Koleksiyonu'nu kullanabilirsiniz.

---