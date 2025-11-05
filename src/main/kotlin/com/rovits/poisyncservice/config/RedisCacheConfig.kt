package com.rovits.poisyncservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.rovits.poisyncservice.domain.dto.PlaceDetails
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration // Spring configuration sınıfı
@EnableCaching // Cache'i aktif et
class RedisCacheConfig {
    private val logger = LoggerFactory.getLogger(RedisCacheConfig::class.java)

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        logger.info("⚙️ Redis Cache Manager yapılandırılıyor...")

        // JSON serialization için ObjectMapper yapılandır
        val objectMapper = ObjectMapper().apply {
            registerKotlinModule() // Kotlin desteği ekle
            registerModule(JavaTimeModule()) // Tarih/saat desteği ekle
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Tarihleri ISO formatında sakla
        }

        // PlaceDetails için JSON serializer oluştur
        val serializer = Jackson2JsonRedisSerializer(objectMapper, PlaceDetails::class.java)

        // Cache yapılandırması
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(24)) // Cache süresi 24 saat
            .serializeKeysWith( // Key'leri String olarak sakla
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith( // Value'ları JSON olarak sakla
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )

        logger.info("💾 Redis Cache yapılandırması:")
        logger.info("   - TTL: 24 saat")
        logger.info("   - Serializer: Jackson2Json (Kotlin + JavaTime)")
        logger.info("   - Cache Name: placeDetails")

        // Cache manager'ı oluştur ve döndür
        val cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build()

        logger.info("✅ Redis Cache Manager hazır")

        return cacheManager
    }
}