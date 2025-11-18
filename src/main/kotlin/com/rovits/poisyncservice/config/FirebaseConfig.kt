package com.rovits.poisyncservice.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.InputStream
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @PostConstruct
    fun initializeFirebase() {
        try {
            val credentialsPath = System.getenv("GOOGLE_CREDENTIALS_PATH")
            val serviceAccount: InputStream?

            if (!credentialsPath.isNullOrBlank()) {
                logger.info("Loading Firebase credentials from file: {}", credentialsPath)
                serviceAccount = FileInputStream(credentialsPath)
            } else {
                logger.info("Loading Firebase credentials from classpath: serviceAccountKey.json")
                serviceAccount = this::class.java.classLoader.getResourceAsStream("serviceAccountKey.json")
            }

            if (serviceAccount == null) {
                logger.error("Firebase credentials not found")
                throw IllegalStateException("Firebase credentials not found")
            }

            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase Admin SDK initialized successfully")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize Firebase Admin SDK", e)
            throw e
        }
    }
}
