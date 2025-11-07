package com.rovits.poisyncservice.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream // YENİ IMPORT
import java.io.InputStream
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun initializeFirebase() {
        try {
            val credentialsPath = System.getenv("GOOGLE_CREDENTIALS_PATH") // 1. Ortam değişkenini kontrol et
            val serviceAccount: InputStream?

            if (!credentialsPath.isNullOrBlank()) {
                // YOL 1: Render.com (veya production) - Dosya yolundan oku
                logger.info("Firebase 'GOOGLE_CREDENTIALS_PATH' ortam değişkeni bulundu. Dosya okunuyor: $credentialsPath")
                serviceAccount = FileInputStream(credentialsPath)
            } else {
                // YOL 2: Lokal Docker Testi - Classpath'ten (resources) oku
                logger.warn("Firebase 'GOOGLE_CREDENTIALS_PATH' ortam değişkeni bulunamadı.")
                logger.info("Classpath'ten (resources) 'serviceAccountKey.json' aranıyor... (Lokal test için uygundur)")
                serviceAccount = this::class.java.classLoader.getResourceAsStream("serviceAccountKey.json")
            }

            if (serviceAccount == null) {
                logger.error("❌ Kritik Hata: Firebase serviceAccountKey.json dosyası ne dosya yolunda ne de classpath'te bulunamadı!")
                throw IllegalStateException("Firebase credentials bulunamadı.")
            }

            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("✅ Firebase Admin SDK başarıyla başlatıldı.")
            }
        } catch (e: Exception) {
            logger.error("❌ Firebase Admin SDK başlatılamadı: ${e.message}", e)
            throw e // Hata durumunda uygulamanın çökmesi (fail-fast) iyidir.
        }
    }
}