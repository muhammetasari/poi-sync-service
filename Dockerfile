# ===================================
# STAGE 1: Build
# ===================================
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle wrapper ve build dosyalarını kopyala (cache için)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Dependency'leri indir (cache layer)
RUN gradle dependencies --no-daemon || true

# Kaynak kodları kopyala
COPY src src

# Uygulamayı build et (testleri atla)
RUN gradle bootJar --no-daemon -x test

# ===================================
# STAGE 2: Runtime (Ubuntu base)
# ===================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Build stage'den JAR'ı kopyala
COPY --from=builder /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Port expose
EXPOSE 8080

# JVM optimizasyonları (Render.com free tier: 512MB RAM için)
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m -Xss256k -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Uygulamayı başlat
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]