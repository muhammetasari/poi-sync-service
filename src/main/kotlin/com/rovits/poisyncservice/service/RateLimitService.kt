package com.rovits.poisyncservice.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class RateLimitService(
    private val redisTemplate: StringRedisTemplate
) {
    private val logger = LoggerFactory.getLogger(RateLimitService::class.java)

    // Lua Script: Anahtarı 1 artır. Eğer yeni değer 1 ise (yani anahtar yeni oluştuysa), süresini ayarla.
    private val script = DefaultRedisScript<Long>(
        "local current = redis.call('INCR', KEYS[1]) " +
                "if current == 1 then " +
                "   redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "end " +
                "return current",
        Long::class.java
    )

    /**
     * İsteğin limite takılıp takılmadığını kontrol eder.
     * @param key Benzersiz anahtar (örn: IP adresi veya User ID)
     * @param limit İzin verilen maksimum istek sayısı
     * @param periodInSeconds Zaman penceresi (saniye cinsinden)
     * @return Eğer limit aşıldıysa true, aşılmadıysa false döner.
     */
    fun isRateLimitExceeded(key: String, limit: Int, periodInSeconds: Long): Boolean {
        val redisKey = "rate_limit:$key"

        try {
            val count = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                periodInSeconds.toString()
            )

            return (count ?: 0L) > limit
        } catch (e: Exception) {
            logger.error("Rate limit check failed for key: $key", e)
            // Redis hatası durumunda trafiği kesmemek için false dönüyoruz (Fail-Open)
            return false
        }
    }
}