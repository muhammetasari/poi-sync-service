package com.rovits.poisyncservice.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.io.InputStream
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun initializeFirebase() {
        try {
            val serviceAccount: InputStream? =
                this::class.java.classLoader.getResourceAsStream("serviceAccountKey.json")

            if (serviceAccount == null) {
                logger.error("❌ Kritik Hata: 'serviceAccountKey.json' dosyası resources altında bulunamadı!")
                throw IllegalStateException("'serviceAccountKey.json' bulunamadı.")
            }

            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            // Firebase'i sadece bir kez başlat
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("✅ Firebase Admin SDK başarıyla başlatıldı.")
            }
        } catch (e: Exception) {
            logger.error("❌ Firebase Admin SDK başlatılamadı: ${e.message}", e)
        }
    }
}