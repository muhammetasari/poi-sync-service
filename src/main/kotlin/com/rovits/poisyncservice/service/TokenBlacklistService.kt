package com.rovits.poisyncservice.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TokenBlacklistService(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val BLACKLIST_PREFIX = "blacklist:"
    }

    /**
     * Token'ı kara listeye ekler.
     * Token'ın kalan ömrü kadar Redis'te tutulur, sonra otomatik silinir.
     */
    fun blacklistToken(token: String, expirationTimeInMillis: Long) {
        val key = BLACKLIST_PREFIX + token
        val ttl = expirationTimeInMillis - System.currentTimeMillis()

        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofMillis(ttl))
        }
    }

    /**
     * Token'ın kara listede olup olmadığını kontrol eder.
     */
    fun isTokenBlacklisted(token: String): Boolean {
        val key = BLACKLIST_PREFIX + token
        return redisTemplate.hasKey(key)
    }
}