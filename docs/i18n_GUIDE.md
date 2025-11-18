# 🌐 i18n_GUIDE.md

Bu rehber, POI Sync Service API için yeni bir dil nasıl eklenir, mevcut mesajlar nasıl güncellenir ve i18n konu başlıkları nasıl yönetilir konularında adım adım açıklamalar içerir.

---

## 1️⃣ Mevcut Dosya Yapısı

Aşağıdaki gibi, her dil için ayrı bir mesaj dosyası bulunur:

```
src/main/resources/
├── messages.properties          # Varsayılan (İngilizce)
├── messages_tr.properties       # Türkçe
├── messages_xx.properties       # Başka diller (Örn: Almanca için messages_de.properties)
```

---

## 2️⃣ Yeni Dil Nasıl Eklenir?

### Adım 1: Dosya Oluştur
Yeni dil eklemek için, o dilin ISO koduna göre bir dosya oluştur:

| Dil        | Dosya Adı                 | ISO Kodu |
|------------|---------------------------|----------|
| Almanca    | messages_de.properties    | de       |
| Fransızca  | messages_fr.properties    | fr       |
| ...        | ...                       | ...      |

### Adım 2: Tüm mesaj anahtarlarını ekle
Yeni dosyada **tüm mesaj anahtarlarını** İngilizce ve Türkçe dosyalardan olduğu gibi oluştur, değerlerini yeni dilde doldur.

**Örnek:**
```properties
error.user.not.found=Benutzer mit E-Mail {0} nicht gefunden
message.operation.successful=Vorgang erfolgreich abgeschlossen
```

### Adım 3: Karakter Coding
Özel karakterli diller için dosya encoding’in **UTF-8** olduğundan emin ol. (IntelliJ'de File > File Encoding)

### Adım 4: Locale Ayarını Gözden Geçir
`MessageSourceConfig.kt` içindeki `supportedLocales` listesini genişletip yeni dil kodunu ekleyebilirsin (yalnızca belirli locale'ları kısıtlamak istiyorsan).  
Otomatik olarak header'dan algılandığı için çoğunlukla eklemeye gerek yoktur.

```kotlin
@Bean
fun localeResolver(): LocaleResolver {
    val resolver = AcceptHeaderLocaleResolver()
    resolver.setDefaultLocale(Locale.ENGLISH)
    resolver.supportedLocales = listOf(Locale.ENGLISH, Locale("tr"), Locale.GERMAN /* yeni eklenen */)
    return resolver
}
```

---

## 3️⃣ Bir Mesajı Güncellemek

1. **Hem default hem tüm lokal dosyalarda** aynı anahtarı aynı şekilde bulup değiştir.
2. Değerini istenen şekilde güncelle.
3. Unutma: Validation mesajındaki placeholderlar (`{0}`) formatına dikkat et!

---

## 4️⃣ Uygulamada Test Etmek

- API isteğinde `Accept-Language: fr` gibi header ile dilediğin dili zorla.
- Eğer header gelmiyorsa varsayılan dil İngilizce olur.

---

## 5️⃣ Sık Karşılaşılan Sorunlar ve Çözümleri

| Sorun                                              | Çözüm/İpucu                                    |
|----------------------------------------------------|------------------------------------------------|
| Dil dosyası UTF-8 kodlanmamışsa Türkçe/özel harf bozuk çıkar | IDE üzerinden encoding'i düzelt                |
| Yeni hata anahtarını bir dilde unutursan           | O alanda key döner, eksik çevirileri tamamla   |
| Validation mesajı `null`/anahtar ile dönüyorsa     | Key iki dosyada da aynı isimde ve doğru mu?    |
| Mesajlar uzun ise satır sonuna `\` ekleme!         | Java *.properties dosyasında satır sonu “\'”    |

---

## 6️⃣ Örnek: Üçüncü Dil (Almanca) Ekleme

1. `messages_de.properties` dosyasını oluştur.
2. İngilizce ve Türkçe dosyalardaki tüm anahtarları kopyala.
3. Karşılıklarını Almanca doldur.
   ```
   error.user.not.found=Benutzer mit E-Mail {0} nicht gefunden
   ```
4. Test: Postman/cURL’de:
   ```
   curl -H "Accept-Language: de" ...
   ```

---

## 7️⃣ Ekstra: i18n'de Placeholder Kullanımı

- `{0}`, `{1}` biçimli placeholderlar dinamik veri içindir.
- Mesaj dosyasına: `error.user.not.found=User not found: {0}`
- Çağrı sırasında: `"User not found: ali@ornek.com"`

---

## 🔗 Kaynaklar

- [Spring Boot i18n Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.internationalization)
- [Java Message Formatting Docs](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html)

---

Herhangi bir ek dil veya çeviri eklemede bu rehber izlenebilir.