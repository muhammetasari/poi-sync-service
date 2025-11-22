# API_RESPONSES.md

Bu dokümanda, POI Sync Service API'nin standart yanıt (response) formatı ve alanları açıklanmaktadır.

---

## Genel Response Formatı

Tüm endpoint'ler başarılı ve hatalı durumlarda standart bir response yapısı döndürür. Başarılı yanıtlar ile hata yanıtları arasında `success` bayrağı ve `data`/`error` alanları kullanılır.

---

### Başarılı Response

```json
{
  "success": true,
  "data": {},
  "timestamp": "2025-11-18T13:00:00"
}
```

#### Alanlar

| Alan      | Tipi    | Açıklama                        |
|-----------|---------|---------------------------------|
| success   | boolean | İşlemin başarılı olup olmadığı  |
| data      | object  | Sonuç/cevap (endpoint'e göre)   |
| timestamp | string  | Response zamanı (ISO-8601)      |

---

### Hatalı Response

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Hata açıklaması"
  },
  "timestamp": "2025-11-18T13:00:00"
}
```

#### Alanlar

| Alan      | Tipi    | Açıklama                        |
|-----------|---------|---------------------------------|
| success   | boolean | İşlemin başarısız olup olmadığı |
| error     | object  | Hata detayları                  |
| timestamp | string  | Response zamanı (ISO-8601)      |

---

## Notlar
- Tüm response'lar JSON formatındadır.
- Hata kodları için [ERROR_CODES.md](../errors/ERROR_CODES.md) dosyasına bakınız.
- Response örnekleri endpoint dokümantasyonunda ayrıca gösterilmiştir.
