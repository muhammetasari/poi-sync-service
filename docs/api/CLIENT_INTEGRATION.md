# 📱 Client (Mobile/Web) Firebase Authentication Entegrasyon Rehberi

Bu dokümanda mobil ve web geliştiriciler için POI Sync Service backend'i ile Firebase Authentication entegrasyonu detaylı şekilde açıklanmaktadır.

---

## 🎯 Genel Bakış

POI Sync Service, Firebase Authentication kullanarak kullanıcı kimlik doğrulamasını yönetir. Client-side akış şu şekildedir:

1. **Client** → Firebase SDK ile kullanıcı kaydı/girişi yapar
2. **Firebase** → ID Token döner
3. **Client** → ID Token'ı backend'e gönderir
4. **Backend** → Token'ı verify eder ve JWT token döner
5. **Client** → JWT token ile backend API'lerini kullanır

---

## 📦 Firebase SDK Kurulumu

### iOS (Swift)

```bash
# CocoaPods
pod 'Firebase/Auth'

# Swift Package Manager
https://github.com/firebase/firebase-ios-sdk
```

### Android (Kotlin)

```gradle
// build.gradle (project level)
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}

// build.gradle (app level)
plugins {
    id 'com.google.gms.google-services'
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth-ktx'
}
```

### Web (JavaScript/TypeScript)

```bash
npm install firebase
# veya
yarn add firebase
```

---

## ⚙️ Firebase Konfigürasyonu

Backend ekibinden alacağınız Firebase config bilgilerini uygulamanıza ekleyin:

### iOS (AppDelegate.swift)

```swift
import Firebase

func application(_ application: UIApplication,
                didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    FirebaseApp.configure()
    return true
}
```

### Android (Application class)

```kotlin
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
```

### Web

```javascript
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIzaSy...",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project-id",
  // ... diğer config bilgileri
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
```

---

## 🔐 Authentication Flow Diyagramı

```
┌─────────┐                ┌──────────┐              ┌─────────┐
│ Client  │                │ Firebase │              │ Backend │
└────┬────┘                └────┬─────┘              └────┬────┘
     │                          │                         │
     │ 1. Register/Login        │                         │
     │─────────────────────────>│                         │
     │                          │                         │
     │ 2. Firebase ID Token     │                         │
     │<─────────────────────────│                         │
     │                          │                         │
     │ 3. POST /api/auth/login  │                         │
     │    (firebaseToken)       │                         │
     │─────────────────────────────────────────────────> │
     │                          │                         │
     │                          │   4. Verify Token       │
     │                          │<────────────────────────│
     │                          │                         │
     │                          │   5. Token Valid        │
     │                          │─────────────────────────>│
     │                          │                         │
     │ 6. JWT Token + User Data │                         │
     │<──────────────────────────────────────────────────│
     │                          │                         │
     │ 7. API Calls (JWT Token) │                         │
     │─────────────────────────────────────────────────> │
     │                          │                         │
```

---

## 1️⃣ Kullanıcı Kaydı (Registration)

### Adım 1: Firebase'de Kullanıcı Oluştur

#### iOS (Swift)

```swift
import FirebaseAuth

func registerUser(email: String, password: String) async throws {
    let authResult = try await Auth.auth().createUser(withEmail: email, password: password)
    let user = authResult.user
    
    // Email doğrulama gönder
    try await user.sendEmailVerification()
    
    // ID Token al
    let idToken = try await user.getIDToken()
    
    // Backend'e gönder
    try await registerWithBackend(firebaseToken: idToken)
}
```

#### Android (Kotlin)

```kotlin
import com.google.firebase.auth.FirebaseAuth

suspend fun registerUser(email: String, password: String) {
    val auth = FirebaseAuth.getInstance()
    
    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
    val user = authResult.user ?: throw Exception("User is null")
    
    // Email doğrulama gönder
    user.sendEmailVerification().await()
    
    // ID Token al
    val idToken = user.getIdToken(false).await().token
    
    // Backend'e gönder
    registerWithBackend(idToken!!)
}
```

#### Web (JavaScript)

```javascript
import { createUserWithEmailAndPassword, sendEmailVerification } from 'firebase/auth';

async function registerUser(email, password) {
  const userCredential = await createUserWithEmailAndPassword(auth, email, password);
  const user = userCredential.user;
  
  // Email doğrulama gönder
  await sendEmailVerification(user);
  
  // ID Token al
  const idToken = await user.getIdToken();
  
  // Backend'e gönder
  await registerWithBackend(idToken);
}
```

### Adım 2: Backend'e Kayıt İsteği Gönder

```http
POST /api/auth/register
Content-Type: application/json
X-API-Key: YOUR_API_KEY

{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "507f1f77bcf86cd799439011",
      "email": "user@example.com",
      "name": "John Doe"
    }
  },
  "timestamp": "2025-11-22T10:30:00"
}
```

---

## 2️⃣ Kullanıcı Girişi (Login)

### Email/Password Login

#### iOS (Swift)

```swift
func loginUser(email: String, password: String) async throws {
    let authResult = try await Auth.auth().signIn(withEmail: email, password: password)
    let user = authResult.user
    
    // Email doğrulanmış mı kontrol et
    guard user.isEmailVerified else {
        throw AuthError.emailNotVerified
    }
    
    // ID Token al
    let idToken = try await user.getIDToken()
    
    // Backend'e gönder
    try await loginWithBackend(firebaseToken: idToken)
}
```

#### Android (Kotlin)

```kotlin
suspend fun loginUser(email: String, password: String) {
    val auth = FirebaseAuth.getInstance()
    
    val authResult = auth.signInWithEmailAndPassword(email, password).await()
    val user = authResult.user ?: throw Exception("User is null")
    
    // Email doğrulanmış mı kontrol et
    if (!user.isEmailVerified) {
        throw Exception("Email not verified")
    }
    
    // ID Token al
    val idToken = user.getIdToken(false).await().token
    
    // Backend'e gönder
    loginWithBackend(idToken!!)
}
```

#### Web (JavaScript)

```javascript
import { signInWithEmailAndPassword } from 'firebase/auth';

async function loginUser(email, password) {
  const userCredential = await signInWithEmailAndPassword(auth, email, password);
  const user = userCredential.user;
  
  // Email doğrulanmış mı kontrol et
  if (!user.emailVerified) {
    throw new Error('Email not verified');
  }
  
  // ID Token al
  const idToken = await user.getIdToken();
  
  // Backend'e gönder
  await loginWithBackend(idToken);
}
```

### Google Sign-In

#### iOS (Swift)

```swift
import GoogleSignIn
import FirebaseAuth

func signInWithGoogle() async throws {
    guard let clientID = FirebaseApp.app()?.options.clientID else { return }
    let config = GIDConfiguration(clientID: clientID)
    GIDSignIn.sharedInstance.configuration = config
    
    let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: getRootViewController())
    let user = result.user
    
    guard let idToken = user.idToken?.tokenString else { return }
    let accessToken = user.accessToken.tokenString
    
    let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: accessToken)
    let authResult = try await Auth.auth().signIn(with: credential)
    
    // Firebase ID Token al
    let firebaseIdToken = try await authResult.user.getIDToken()
    
    // Backend'e gönder
    try await loginWithBackend(firebaseToken: firebaseIdToken)
}
```

#### Android (Kotlin)

```kotlin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider

suspend fun signInWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
    val user = authResult.user ?: throw Exception("User is null")
    
    // Firebase ID Token al
    val firebaseIdToken = user.getIdToken(false).await().token
    
    // Backend'e gönder
    loginWithBackend(firebaseIdToken!!)
}
```

#### Web (JavaScript)

```javascript
import { signInWithPopup, GoogleAuthProvider } from 'firebase/auth';

async function signInWithGoogle() {
  const provider = new GoogleAuthProvider();
  const result = await signInWithPopup(auth, provider);
  const user = result.user;
  
  // Firebase ID Token al
  const idToken = await user.getIdToken();
  
  // Backend'e gönder
  await loginWithBackend(idToken);
}
```

### Backend'e Login İsteği

```http
POST /api/auth/login
Content-Type: application/json
X-API-Key: YOUR_API_KEY

{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

**Response (200 OK):** Register ile aynı format

---

## 3️⃣ Şifre Sıfırlama

### Adım 1: Şifre Sıfırlama Email'i Gönder

#### iOS (Swift)

```swift
func sendPasswordReset(email: String) async throws {
    try await Auth.auth().sendPasswordReset(withEmail: email)
    // Kullanıcıya email gönderildiğini bildir
}
```

#### Android (Kotlin)

```kotlin
suspend fun sendPasswordReset(email: String) {
    FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
    // Kullanıcıya email gönderildiğini bildir
}
```

#### Web (JavaScript)

```javascript
import { sendPasswordResetEmail } from 'firebase/auth';

async function sendPasswordReset(email) {
  await sendPasswordResetEmail(auth, email);
  // Kullanıcıya email gönderildiğini bildir
}
```

### Adım 2: Backend'den Email Gönder (Opsiyonel)

Backend'den de şifre sıfırlama email'i gönderilebilir:

```http
POST /api/auth/send-password-reset-email
Content-Type: application/json
X-API-Key: YOUR_API_KEY

{
  "email": "user@example.com"
}
```

---

## 4️⃣ Email Doğrulama

### Email Doğrulama Gönder

#### iOS (Swift)

```swift
func sendEmailVerification() async throws {
    guard let user = Auth.auth().currentUser else { return }
    try await user.sendEmailVerification()
}
```

#### Android (Kotlin)

```kotlin
suspend fun sendEmailVerification() {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    user.sendEmailVerification().await()
}
```

#### Web (JavaScript)

```javascript
import { sendEmailVerification } from 'firebase/auth';

async function sendEmailVerificationEmail() {
  const user = auth.currentUser;
  if (user) {
    await sendEmailVerification(user);
  }
}
```

### Email Doğrulama Durumunu Kontrol

```swift
// iOS
let isVerified = Auth.auth().currentUser?.isEmailVerified ?? false

// Android
val isVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false

// Web
const isVerified = auth.currentUser?.emailVerified ?? false;
```

---

## 5️⃣ Token Yenileme (Refresh)

Firebase ID Token'lar 1 saat sonra expire olur. Backend JWT token'ınız da expire olabilir.

### Firebase Token Refresh

#### iOS (Swift)

```swift
func refreshFirebaseToken() async throws -> String {
    guard let user = Auth.auth().currentUser else {
        throw AuthError.userNotFound
    }
    return try await user.getIDToken(forcingRefresh: true)
}
```

#### Android (Kotlin)

```kotlin
suspend fun refreshFirebaseToken(): String {
    val user = FirebaseAuth.getInstance().currentUser
        ?: throw Exception("User not found")
    return user.getIdToken(true).await().token ?: throw Exception("Token is null")
}
```

#### Web (JavaScript)

```javascript
async function refreshFirebaseToken() {
  const user = auth.currentUser;
  if (!user) throw new Error('User not found');
  return await user.getIdToken(true);
}
```

### Backend JWT Token Refresh

Backend refresh token kullanarak yeni JWT alabilirsiniz. Şu anda backend'de refresh endpoint'i yok, ancak eklenebilir:

```http
POST /api/auth/refresh
Content-Type: application/json
X-API-Key: YOUR_API_KEY

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 6️⃣ Logout

### Firebase Logout

#### iOS (Swift)

```swift
func logout() throws {
    try Auth.auth().signOut()
    // Backend'e logout isteği gönder
    logoutFromBackend()
}
```

#### Android (Kotlin)

```kotlin
fun logout() {
    FirebaseAuth.getInstance().signOut()
    // Backend'e logout isteği gönder
    logoutFromBackend()
}
```

#### Web (JavaScript)

```javascript
import { signOut } from 'firebase/auth';

async function logout() {
  await signOut(auth);
  // Backend'e logout isteği gönder
  await logoutFromBackend();
}
```

### Backend Logout

```http
POST /api/auth/logout
Content-Type: application/json
X-API-Key: YOUR_API_KEY
Authorization: Bearer YOUR_JWT_TOKEN

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 7️⃣ Role-Based UI (User vs Admin)

Firebase Custom Claims'den user role'ünü okuyup UI'ı buna göre ayarlayabilirsiniz.

### Role Okuma

#### iOS (Swift)

```swift
func getUserRole() async throws -> String {
    guard let user = Auth.auth().currentUser else {
        throw AuthError.userNotFound
    }
    
    let result = try await user.getIDTokenResult(forcingRefresh: false)
    return result.claims["role"] as? String ?? "user"
}
```

#### Android (Kotlin)

```kotlin
suspend fun getUserRole(): String {
    val user = FirebaseAuth.getInstance().currentUser
        ?: throw Exception("User not found")
    
    val result = user.getIdToken(false).await()
    return result.claims["role"] as? String ?: "user"
}
```

#### Web (JavaScript)

```javascript
async function getUserRole() {
  const user = auth.currentUser;
  if (!user) throw new Error('User not found');
  
  const idTokenResult = await user.getIdTokenResult();
  return idTokenResult.claims.role || 'user';
}
```

### UI Koşullu Render

```swift
// iOS
if await getUserRole() == "admin" {
    // Admin paneli göster
}

// Android
if (getUserRole() == "admin") {
    // Admin paneli göster
}

// Web
if (await getUserRole() === 'admin') {
    // Admin paneli göster
}
```

---

## 8️⃣ Error Handling

### Firebase Error Codes

```swift
// iOS
do {
    try await loginUser(email: email, password: password)
} catch let error as NSError {
    switch AuthErrorCode(_nsError: error).code {
    case .invalidEmail:
        print("Geçersiz email")
    case .wrongPassword:
        print("Yanlış şifre")
    case .userNotFound:
        print("Kullanıcı bulunamadı")
    case .emailAlreadyInUse:
        print("Email zaten kullanımda")
    case .weakPassword:
        print("Zayıf şifre")
    default:
        print("Hata: \(error.localizedDescription)")
    }
}
```

### Backend Error Handling

Backend'den dönen error response'ları:

```json
{
  "success": false,
  "error": {
    "code": "AUTH_005",
    "message": "Geçersiz Firebase kimlik doğrulama token'ı",
    "timestamp": "2025-11-22T10:30:00"
  }
}
```

**Yaygın Error Kodları:**
- `AUTH_001` - Token expired
- `AUTH_002` - Invalid token
- `AUTH_005` - Invalid Firebase token
- `AUTH_006` - Email not verified
- `USER_002` - User already exists
- `VAL_001` - Validation error

---

## 9️⃣ Backend API Kullanımı

### JWT Token ile API Çağrıları

Tüm korumalı endpoint'ler için JWT token gereklidir:

```http
GET /api/places/nearby?lat=41.0082&lng=28.9784&radius=1000
Authorization: Bearer YOUR_JWT_TOKEN
X-API-Key: YOUR_API_KEY
Accept-Language: tr
```

### Örnek HTTP Client

#### iOS (URLSession)

```swift
func makeAuthenticatedRequest(endpoint: String, jwtToken: String) async throws {
    var request = URLRequest(url: URL(string: "https://api.yourdomain.com\(endpoint)")!)
    request.setValue("Bearer \(jwtToken)", forHTTPHeaderField: "Authorization")
    request.setValue(API_KEY, forHTTPHeaderField: "X-API-Key")
    request.setValue("tr", forHTTPHeaderField: "Accept-Language")
    
    let (data, response) = try await URLSession.shared.data(for: request)
    // Response işle
}
```

#### Android (Retrofit)

```kotlin
interface ApiService {
    @GET("/api/places/nearby")
    suspend fun getNearbyPlaces(
        @Header("Authorization") auth: String,
        @Header("X-API-Key") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int
    ): Response<ApiResponse<List<Place>>>
}

// Kullanım
val response = apiService.getNearbyPlaces(
    auth = "Bearer $jwtToken",
    apiKey = API_KEY,
    lat = 41.0082,
    lng = 28.9784,
    radius = 1000
)
```

---

## 🔟 Best Practices

### 1. Token Storage (Güvenli Saklama)

**iOS:**
```swift
// Keychain kullanın
let keychain = KeychainSwift()
keychain.set(jwtToken, forKey: "jwt_token")
```

**Android:**
```kotlin
// EncryptedSharedPreferences kullanın
val encryptedPrefs = EncryptedSharedPreferences.create(/*...*/)
encryptedPrefs.edit().putString("jwt_token", jwtToken).apply()
```

**Web:**
```javascript
// HttpOnly cookie (backend tarafından set edilmeli) veya sessionStorage
sessionStorage.setItem('jwt_token', jwtToken);
```

### 2. Token Expiry Handling

JWT token expire olduğunda otomatik refresh yapın:

```swift
// iOS - Interceptor pattern
if response.statusCode == 401 {
    let newFirebaseToken = try await refreshFirebaseToken()
    let newJWT = try await loginWithBackend(firebaseToken: newFirebaseToken)
    // İsteği tekrar dene
}
```

### 3. Network Error Handling

```swift
do {
    let response = try await apiCall()
} catch {
    if let urlError = error as? URLError {
        switch urlError.code {
        case .notConnectedToInternet:
            print("İnternet bağlantısı yok")
        case .timedOut:
            print("İstek zaman aşımına uğradı")
        default:
            print("Ağ hatası")
        }
    }
}
```

### 4. Email Verification Check

Her login'de email doğrulama kontrolü yapın:

```swift
if !user.isEmailVerified {
    // Email doğrulama ekranına yönlendir
    showEmailVerificationScreen()
}
```

---

## 📚 Örnek Repository Yapısı

```
/app
  /data
    /remote
      AuthApi.kt
      ApiService.kt
    /local
      TokenManager.kt
  /domain
    /model
      User.kt
      AuthResponse.kt
    /repository
      AuthRepository.kt
  /ui
    /auth
      LoginScreen.kt
      RegisterScreen.kt
      /viewmodel
        AuthViewModel.kt
```

---

## 🆘 Sorun Giderme

### Firebase Token Alınamıyor
- Firebase SDK'nın doğru configure edildiğinden emin olun
- Internet bağlantısını kontrol edin
- Firebase Console'da user'ın var olduğunu kontrol edin

### Backend "Invalid Token" Hatası
- Firebase token'ın expire olmadığını kontrol edin (1 saat)
- Token'ın tam olarak gönderildiğinden emin olun
- X-API-Key header'ının eklendiğini kontrol edin

### Email Doğrulama Maili Gelmiyor
- Spam klasörünü kontrol edin
- Firebase Console'da email template'lerinin aktif olduğunu kontrol edin
- Email adresinin doğru olduğundan emin olun

---

## 📞 İletişim

Backend API ile ilgili sorularınız için backend ekibi ile iletişime geçin.

**API Base URL:**
- Development: `http://localhost:8080`
- Production: `https://api.yourdomain.com`

**API Dokümantasyonu:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Endpoints: [ENDPOINTS.md](ENDPOINTS.md)

---

**Happy Coding! 🚀**

