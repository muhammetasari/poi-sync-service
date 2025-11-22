# FIREBASE_SETUP.md

Bu dokümanda POI Sync Service için Firebase Authentication kurulumu ve konfigürasyonu adım adım açıklanmaktadır.

---

## Ön Hazırlık

1. [Firebase Console](https://console.firebase.google.com/) hesabınıza giriş yapın
2. Yeni bir proje oluşturun veya mevcut projenizi seçin

---

## Firebase Authentication Aktifleştirme

### Adım 1: Authentication'ı Etkinleştirin

1. Firebase Console'da sol menüden **Build > Authentication** seçeneğine tıklayın
2. **Get Started** butonuna tıklayın
3. Authentication sayfası açılacaktır

### Adım 2: Sign-in Method'ları Yapılandırın

**Email/Password Provider:**

1. **Sign-in method** sekmesine gidin
2. **Email/Password** satırına tıklayın
3. **Enable** toggle'ını açın
4. **Email link (passwordless sign-in)** seçeneğini **kapalı** bırakın (opsiyonel)
5. **Save** butonuna tıklayın

**Google Provider:**

1. **Sign-in method** sekmesinde **Google** satırına tıklayın
2. **Enable** toggle'ını açın
3. **Project support email** seçin (Firebase projenizin email'i')
4. **Save** butonuna tıklayın

**Facebook Provider (Opsiyonel):**

1. [Facebook Developers](https://developers.facebook.com/) hesabınızda bir uygulama oluşturun
2. Facebook App ID ve App Secret'i alın
3. Firebase Console'da **Facebook** satırına tıklayın
4. **Enable** toggle'ını açın
5. App ID ve App Secret'i girin
6. OAuth redirect URI'yi Facebook uygulamanıza ekleyin
7. **Save** butonuna tıklayın

**Apple Provider (Opsiyonel):**

1. [Apple Developer](https://developer.apple.com/) hesabınızda bir uygulama oluşturun
2. Apple Service ID ve Secret Key alın
3. Firebase Console'da **Apple** satırına tıklayın
4. **Enable** toggle'ını açın
5. Gerekli alanları doldurun
6. **Save** butonuna tıklayın

---

## Service Account Key Oluşturma

1. Firebase Console'da **Project Settings > Service Accounts** sekmesine gidin
2. **Generate new private key** butonuna tıklayın
3. Oluşan JSON dosyasını backend projenize ekleyin (ör: `src/main/resources/serviceAccountKey.json`)

---

## Güvenlik ve Quota Ayarları

- Email template'lerini özelleştirin (Türkçe/İngilizce)
- Password policy ayarlarını kontrol edin (min 8 karakter, karmaşık şifre önerilir)
- Gerekirse domain yetkilendirmelerini ekleyin
- Quota ve rate limit ayarlarını gözden geçirin

---

## Sorun Giderme

- Firebase Console'da ilgili hata mesajlarını inceleyin
- Service Account Key'in doğru yüklendiğinden emin olun
- API anahtarlarının ve environment değişkenlerinin eksiksiz olduğuna dikkat edin

---

## İlgili Dokümanlar
- [CLIENT_INTEGRATION.md](../api/CLIENT_INTEGRATION.md)
- [AUTH_MIGRATION_SUMMARY.md](../auth/AUTH_MIGRATION_SUMMARY.md)
