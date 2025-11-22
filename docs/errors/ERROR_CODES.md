# ERROR_CODES.md

Bu dokümanda, POI Sync Service içinde kullanılan hata kodları, bunların kısa açıklamaları ve ilişkili i18n mesaj anahtarları listelenmiştir.

Her hata kodu, sistemdeki hata tipini kolay anlaşılır ve takip edilebilir kılmak için belirli bir kategoriyle (USER, AUTH, VAL, EXT, DB, CACHE, SYS) başlar.

---

## Kullanıcı Hataları (USER_xxx)

| Kod           | Mesaj Anahtarı             | Açıklama                                    |
|---------------|----------------------------|----------------------------------------------|
| USER_001      | error.user.not.found       | Kullanıcı bulunamadı                        |
| USER_002      | error.user.already.exists  | Bu email/e-posta zaten kullanılıyor         |
| USER_003      | error.invalid.credentials  | Geçersiz kimlik bilgileri                   |
| USER_004      | error.user.creation.failed | Kullanıcı oluşturulamadı                    |
| USER_005      | error.user.update.failed   | Kullanıcı güncellenemedi                    |

---

## Kimlik Doğrulama Hataları (AUTH_xxx)

| Kod           | Mesaj Anahtarı                  | Açıklama                              |
|---------------|---------------------------------|----------------------------------------|
| AUTH_001      | error.token.expired             | Token süresi dolmuş                    |
| AUTH_002      | error.token.invalid             | Token geçersiz                        |
| AUTH_003      | error.unauthorized              | Yetkisiz erişim                        |
| AUTH_004      | error.access.denied             | Erişim reddedildi                     |
| AUTH_005      | error.firebase.token.invalid    | Firebase token geçersiz                |
| AUTH_006      | error.jwt.generation.failed     | JWT token üretilemedi                  |
| AUTH_007      | error.missing.authentication    | Kimlik doğrulama bilgisi eksik         |
| AUTH_008      | error.too.many.requests         | Rate limit aşıldı                      |
| AUTH_009      | error.auth.email.not.verified   | E-posta doğrulanmamış                  |
| AUTH_010      | error.auth.provider.mismatch    | Sağlayıcı uyumsuzluğu                  |
| AUTH_011      | error.validation.invalid.token  | Token geçersiz veya bozulmuş           |
| AUTH_012      | error.email.already.verified    | E-posta zaten doğrulanmış              |

---

## Validasyon Hataları (VAL_xxx)

| Kod           | Mesaj Anahtarı                        | Açıklama                                      |
|---------------|---------------------------------------|------------------------------------------------|
| VAL_001       | error.validation.failed               | Genel validasyon hatası                        |
| VAL_002       | error.validation.email                | Email formatı/şablonu hatalı                   |
| VAL_003       | error.validation.password             | Şifre en az 8 karakter                        |
| VAL_004       | error.validation.required             | Zorunlu alan eksik                             |
| VAL_005       | error.validation.latitude             | Enlem değeri geçersiz                          |
| VAL_006       | error.validation.longitude            | Boylam değeri geçersiz                         |
| VAL_007       | error.validation.radius               | Yarıçap değeri geçersiz                        |
| VAL_008       | error.validation.date.range           | Tarih aralığı hatası                            |
| VAL_009       | error.validation.name.size            | İsim uzunluk hatası                             |
| VAL_010       | error.validation.password.min         | Şifre minimum uzunluk hatası                    |
| VAL_011       | error.validation.password.strength    | Şifre karmaşıklık/güçlük hatası                |
| VAL_012       | error.validation.provider.invalid     | Sağlayıcı (provider) değeri yanlış              |
| VAL_013       | error.validation.password.policy      | Şifre politikası hatası                         |

---

## Harici Servis Hataları (EXT_xxx)

| Kod           | Mesaj Anahtarı                   | Açıklama                                        |
|---------------|----------------------------------|--------------------------------------------------|
| EXT_001       | error.google.api.failed          | Google API isteği başarısız oldu                 |
| EXT_002       | error.google.api.unavailable     | Google API şu anda kullanılamıyor                |
| EXT_003       | error.google.api.rate.limit      | Google API rate limit aşıldı                     |
| EXT_004       | error.firebase.failed            | Firebase servis hatası                           |
| EXT_005       | error.firebase.unavailable       | Firebase servisi şu anda kullanılamıyor          |
| EXT_006       | error.external.service.timeout   | Harici servis zaman aşımı                        |

---

## Veritabanı Hataları (DB_xxx)

| Kod           | Mesaj Anahtarı                     | Açıklama                                          |
|---------------|------------------------------------|----------------------------------------------------|
| DB_001        | error.database.failed              | Genel veritabanı hatası                            |
| DB_002        | error.database.unavailable         | Veritabanı servisi geçici olarak kullanılamıyor    |
| DB_003        | error.database.connection.failed   | Veritabanına bağlanılamadı                         |
| DB_004        | error.database.duplicate.key       | Tekil (unique) anahtar çakıştı                     |
| DB_005        | error.database.timeout             | Veritabanı işleminde zaman aşımı                   |

---

## Cache Hataları (CACHE_xxx)

| Kod           | Mesaj Anahtarı                         | Açıklama                                           |
|---------------|----------------------------------------|-----------------------------------------------------|
| CACHE_001     | error.cache.operation.failed           | Genel cache işlemi hatası                           |
| CACHE_002     | error.cache.unavailable                | Cache servisi kullanılamıyor                        |
| CACHE_003     | error.cache.serialization.failed       | Cache verisi serileştirilemedi                      |
| CACHE_004     | error.cache.connection.failed          | Cache servisine bağlantı hatası                     |

---

## POI Hataları (POI_xxx)

| Kod           | Mesaj Anahtarı                         | Açıklama                                      |
|---------------|----------------------------------------|-----------------------------------------------|
| POI_001       | error.poi.not.found                    | POI (ilgi noktası) bulunamadı                 |
| POI_002       | error.poi.sync.failed                  | POI senkronizasyonu başarısız                 |
| POI_003       | error.poi.invalid.type                 | Geçersiz POI tipi                             |
| POI_004       | error.poi.search.failed                | POI araması başarısız                         |

---

## Sistem Hataları (SYS_xxx)

| Kod           | Mesaj Anahtarı                 | Açıklama                              |
|---------------|-------------------------------|----------------------------------------|
| SYS_001       | error.internal.server         | Sunucu (internal server) hatası        |
| SYS_002       | error.service.unavailable     | Servis geçici olarak kullanılamıyor    |
| SYS_003       | error.configuration           | Genel sistem yapılandırma hatası       |
| SYS_999       | error.unknown                 | Bilinmeyen hata                        |

---

> **Not:** Ek hata kodları eklendikçe bu dokümanı güncelleyiniz!

---