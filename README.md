# POI Sync Service

POI (Point of Interest) senkronizasyon servisi, Google Places API ile entegre çalışan, Spring Boot tabanlı bir backend uygulamasıdır.

## 🚀 Kullanılan Teknolojiler

### Core Framework & Language
- **Kotlin 1.9.25** - Ana programlama dili
- **Spring Boot 3.5.7** - Backend framework
- **Java 17** - JVM runtime
- **Gradle 8.5** - Build tool & dependency management

### Spring Boot Modules
- **Spring Web** - RESTful web servisleri
- **Spring WebFlux** - Reaktif web programlama
- **Spring Security** - Güvenlik ve kimlik doğrulama
- **Spring Data MongoDB** - MongoDB veri erişimi
- **Spring Data Redis** - Redis cache yönetimi
- **Spring Cache** - Cache abstraction
- **Spring Actuator** - Health check ve monitoring
- **Spring AOP** - Aspect Oriented Programming

### Database & Cache
- **MongoDB** - NoSQL veritabanı (MongoDB Atlas cloud)
- **Redis** - Cache ve in-memory data store (Upstash Cloud)

### External Services & APIs
- **Firebase Admin SDK 9.2.0** - Firebase entegrasyonu
- **Google Places API** - POI bilgileri

### Security & Authentication
- **JWT (JSON Web Tokens)** - Token tabanlı kimlik doğrulama
  - `jjwt-api:0.12.5`
  - `jjwt-impl:0.12.5`
  - `jjwt-jackson:0.12.5`
- **Spring Security** - Uygulama güvenliği

### Reactive Programming
- **Kotlin Coroutines** - Asenkron programlama
- **Project Reactor** - Reaktif stream implementasyonu
- **Reactor Kotlin Extensions** - Kotlin için reactor uzantıları

### JSON Processing
- **Jackson Module Kotlin** - Kotlin için JSON serialization/deserialization

### Testing
- **JUnit 5** - Test framework
- **MockK 1.13.8** - Kotlin için mocking library
- **SpringMockK 4.0.2** - Spring için MockK entegrasyonu
- **Testcontainers 1.19.3** - Container tabanlı integration testler
  - MongoDB Testcontainers
  - JUnit Jupiter integration
- **WireMock 3.3.1** - HTTP API mocking
- **Reactor Test** - Reactive streams test
- **Spring Security Test** - Security test utilities
- **Kotlin Test JUnit5** - Kotlin test utilities
- **Kotlinx Coroutines Test** - Coroutines test

### DevOps & Deployment
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Eclipse Temurin 17 JRE** - Production runtime image

## 📋 Özellikler

- ✅ RESTful API endpoints
- ✅ Reaktif programlama desteği
- ✅ JWT tabanlı authentication
- ✅ Redis ile caching (24 saat TTL)
- ✅ MongoDB ile veri persistance
- ✅ Firebase entegrasyonu
- ✅ Google Places API entegrasyonu
- ✅ Health check endpoints
- ✅ Docker containerization
- ✅ SSL/TLS desteği (Redis)
- ✅ Kapsamlı test coverage

## 🛠️ Kurulum

### Gereksinimler
- Java 17 veya üzeri
- Docker & Docker Compose (opsiyonel)
- MongoDB Atlas hesabı
- Upstash Redis hesabı
- Google API Key

### Environment Variables

```bash
# Redis (Upstash)
SPRING_DATA_REDIS_HOST=your-redis-host
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=your-redis-password
SPRING_DATA_REDIS_SSL=true

# MongoDB (Atlas)
MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/database

# Google API
GOOGLE_API_KEY=your-google-api-key

# Security
API_SECRET_KEY=your-api-secret
JWT_SECRET=your-jwt-secret

# Application
SPRING_PROFILES_ACTIVE=docker
LOGGING_LEVEL_COM_ROVITS=INFO
```

### Yerel Geliştirme

```bash
# Projeyi klonlayın
git clone <repository-url>
cd poi-sync-service

# Build
./gradlew build

# Çalıştır
./gradlew bootRun
```

### Docker ile Çalıştırma

```bash
# Docker Compose ile
docker-compose up -d

# Sadece build
docker build -t poi-sync-service .

# Container çalıştır
docker run -p 8080:8080 --env-file .env poi-sync-service
```

## 📡 API Endpoints

### Health Check
```
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## 🏗️ Proje Yapısı

```
src/main/kotlin/com/rovits/poisyncservice/
├── client/          # External API clients
├── config/          # Configuration classes
├── controller/      # REST controllers
├── domain/          # Domain models
├── repository/      # Data repositories
├── service/         # Business logic
└── PoiSyncServiceApplication.kt
```

## 🔒 Güvenlik

- JWT token bazlı authentication
- Spring Security ile endpoint koruması
- API key doğrulama
- Redis SSL/TLS bağlantısı
- Hassas bilgiler için environment variables

## 📊 Cache Stratejisi

- **Cache Provider**: Redis (Upstash Cloud)
- **Cache Name**: `placeDetails`
- **TTL**: 24 saat
- **SSL**: Etkin

## 🧪 Test

```bash
# Tüm testleri çalıştır
./gradlew test

# Integration testler (Testcontainers ile)
./gradlew integrationTest

# Test coverage raporu
./gradlew jacocoTestReport
```

## 📝 Lisans

[Lisans bilgisi buraya eklenecek]

## 👥 Katkıda Bulunma

[Katkıda bulunma kuralları buraya eklenecek]

## 📧 İletişim

[İletişim bilgileri buraya eklenecek]

