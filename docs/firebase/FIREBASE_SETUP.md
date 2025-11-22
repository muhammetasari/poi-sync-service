# 🔥 Firebase Console Kurulum Rehberi

Bu dokümanda POI Sync Service için Firebase Authentication kurulumu ve konfigürasyonu adım adım açıklanmaktadır.

---

## 📋 Ön Hazırlık

1. [Firebase Console](https://console.firebase.google.com/) hesabınıza giriş yapın
2. Yeni bir proje oluşturun veya mevcut projenizi seçin

---

## 1️⃣ Firebase Authentication Aktifleştirme

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
3. **Project support email** seçin (Firebase projenizin email'i)
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

1. [Apple Developer](https://developer.apple.com/) hesabınızda Services ID yapılandırın
2. Firebase Console'da **Apple** satırına tıklayın
3. **Enable** toggle'ını açın
4. Service ID, Team ID, Key ID ve Private Key bilgilerini girin
5. **Save** butonuna tıklayın

---

## 2️⃣ Email Template'lerini Özelleştirme

### Şifre Sıfırlama Email Template

1. **Authentication > Templates** sekmesine gidin
2. **Password reset** satırına tıklayın
3. **Edit template** butonuna tıklayın

**Türkçe Template (Varsayılan):**

```
Konu: Şifrenizi Sıfırlayın

Merhaba,

POI Sync Service hesabınız için şifre sıfırlama talebinde bulundunuz. Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:

%LINK%

Bu talebi siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.

Teşekkürler,
POI Sync Service Ekibi
```

### Email Doğrulama Template

1. **Authentication > Templates** sekmesinde **Email address verification** satırına tıklayın
2. **Edit template** butonuna tıklayın

**Türkçe Template:**

```
Konu: E-posta Adresinizi Doğrulayın

Merhaba,

POI Sync Service hesabınıza hoş geldiniz! E-posta adresinizi doğrulamak için aşağıdaki bağlantıya tıklayın:

%LINK%

E-posta adresiniz doğrulandıktan sonra tüm özelliklere erişebileceksiniz.

Teşekkürler,
POI Sync Service Ekibi
```

---

## 3️⃣ Firebase Custom Claims - Role Yönetimi

Firebase Custom Claims, kullanıcılara özel metadata (örn: roller, izinler) eklemek için kullanılır.

### Custom Claims Nedir?

- Firebase ID Token içinde custom data saklama mekanizması
- Backend'de authorization için kullanılır
- Max 1000 byte veri saklanabilir
- Client-side erişilebilir (JWT içinde)

### Backend'de Custom Claims Ayarlama

POI Sync Service, kullanıcı kaydı sırasında otomatik olarak `role: "user"` claim'i atar.

**Default Roles:**
- `user` - Normal kullanıcı (default)
- `admin` - Yönetici (manuel atama gerektirir)

### İlk Admin Kullanıcı Oluşturma

**Yöntem 1: Firebase Console + Backend API**

1. Firebase Console'da **Authentication > Users** sekmesinden kullanıcı oluşturun
2. Kullanıcının **UID**'sini kopyalayın
3. Backend'de geliştirme ortamında aşağıdaki komutu çalıştırın:

```bash
# Environment variable ile ilk admin email'i set edin
export INITIAL_ADMIN_EMAIL="admin@yourdomain.com"
```

4. Backend uygulama başlarken otomatik olarak bu kullanıcıya admin role'ü atanacak

**Yöntem 2: Manuel Backend API Çağrısı**

```bash
# JWT token ile admin endpoint'e istek atın
curl -X PUT http://localhost:8080/api/auth/users/{userId}/role \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "X-API-Key: YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"role": "admin"}'
```

> **Not:** İlk admin kullanıcıyı oluşturmak için geliştirme ortamında geçici bir endpoint veya initialization script kullanabilirsiniz.

---

## 4️⃣ Service Account Key Oluşturma (Backend İçin)

Backend'in Firebase Admin SDK kullanması için servis hesabı anahtarı gereklidir.

### Adım 1: Service Account Oluşturun

1. Firebase Console'da **Project Settings** (⚙️ ikonu) > **Service accounts** sekmesine gidin
2. **Generate new private key** butonuna tıklayın
3. Onay penceresinde **Generate key** butonuna tıklayın
4. JSON dosyası indirilecektir

### Adım 2: Service Account Key'i Backend'e Ekleyin

**Lokal Geliştirme:**

1. İndirilen JSON dosyasını `src/main/resources/serviceAccountKey.json` olarak kaydedin
2. `.gitignore` dosyasında bu dosyanın ignore edildiğinden emin olun

**Production/Docker:**

1. JSON dosyasını güvenli bir yere kaydedin
2. `GOOGLE_CREDENTIALS_PATH` environment variable'ını JSON dosyasının yoluna set edin:

```bash
export GOOGLE_CREDENTIALS_PATH=/path/to/serviceAccountKey.json
```

Veya Docker Compose'da:

```yaml
environment:
  GOOGLE_CREDENTIALS_PATH: /app/config/serviceAccountKey.json
volumes:
  - ./serviceAccountKey.json:/app/config/serviceAccountKey.json:ro
```

---

## 5️⃣ Security Rules ve Quotas

### Password Policy

1. **Authentication > Settings > Password policy** sekmesine gidin
2. Minimum şifre uzunluğu ayarlayın (önerilen: 8 karakter)
3. **Require uppercase**, **require lowercase**, **require number** seçeneklerini aktifleştirin

> **Not:** Client-side Firebase SDK bu politikaları otomatik kontrol eder.

### Rate Limiting

Firebase Authentication için default rate limit'ler mevcuttur:
- Email/Password Sign-up: 100/saat (per IP)
- Sign-in attempts: 1000/saat (per IP)

Ek koruma için Firebase App Check kullanabilirsiniz.

---

## 6️⃣ Firebase Web API Key

Client SDK'lar için Firebase Web API Key gereklidir:

1. **Project Settings** > **General** sekmesine gidin
2. **Your apps** bölümünde Web app (</>) butonuna tıklayın
3. App'inize bir isim verin ve kaydedin
4. **Firebase configuration** bilgilerini kopyalayın:

```javascript
const firebaseConfig = {
  apiKey: "AIzaSy...",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project-id",
  storageBucket: "your-project.appspot.com",
  messagingSenderId: "123456789",
  appId: "1:123456789:web:abcdef"
};
```

**Bu bilgileri mobil geliştirme ekibine iletin.**

---

## 7️⃣ Authorized Domains

Production domain'inizi Firebase'e ekleyin:

1. **Authentication > Settings > Authorized domains** sekmesine gidin
2. **Add domain** butonuna tıklayın
3. Production domain'inizi ekleyin (örn: `yourdomain.com`)

---

## 8️⃣ İzleme ve Loglama

### Authentication Logs

1. **Authentication > Users** sekmesinde kullanıcı aktivitelerini görüntüleyin
2. Her kullanıcının **Last sign-in** ve **Created** bilgilerini kontrol edin

### Firebase Console Monitoring

1. **Analytics** sekmesinde kullanıcı davranışlarını izleyin
2. **Authentication** dashboard'unda günlük/aylık sign-up/sign-in metriklerini görüntüleyin

---

## 🔟 Güvenlik Kontrol Listesi

- ✅ Service Account Key `.gitignore`'da
- ✅ Production domain'i Authorized domains'e eklendi
- ✅ Email template'leri özelleştirildi (Türkçe)
- ✅ Password policy ayarlandı (min 8 char, uppercase, lowercase, number)
- ✅ Email/Password provider aktif
- ✅ Google provider aktif (diğer sosyal login'ler opsiyonel)
- ✅ İlk admin kullanıcısı oluşturuldu
- ✅ Custom claims mekanizması anlaşıldı

---

## 📚 Ek Kaynaklar

- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [Firebase Admin SDK - Custom Claims](https://firebase.google.com/docs/auth/admin/custom-claims)
- [Firebase Custom Claims Best Practices](https://firebase.google.com/docs/auth/admin/custom-claims#best_practices)
- [Firebase Security Rules](https://firebase.google.com/docs/rules)

---

## 🆘 Sorun Giderme

### "Invalid API Key" Hatası
- Web API Key'in doğru olduğundan emin olun
- Authorized domains listesini kontrol edin

### Email Gönderilmiyor
- Firebase Console > Authentication > Templates'te email template'lerinin aktif olduğunu kontrol edin
- Spam klasörünü kontrol edin
- Firebase projesinin email gönderim limitlerini aşmadığından emin olun

### Service Account Key Bulunamıyor
- `GOOGLE_CREDENTIALS_PATH` environment variable'ının doğru set edildiğinden emin olun
- JSON dosyasının doğru formatta olduğunu kontrol edin
- Backend loglarında Firebase initialization hatalarını kontrol edin

### Custom Claims Güncellenmedi
- Client-side token refresh yapıldığından emin olun: `user.getIdToken(true)`
- Token cache'i temizleyin
- Backend'de custom claims set edilirken hata olmadığını kontrol edin

---

**Kurulum tamamlandı! 🎉**

Şimdi mobil geliştirme ekibi için [CLIENT_INTEGRATION.md](CLIENT_INTEGRATION.md) dökümanına geçebilirsiniz.

