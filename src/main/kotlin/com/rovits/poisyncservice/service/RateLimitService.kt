package com.rovits.poisyncservice.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class RateLimitService {
    private val attempts = ConcurrentHashMap<String, Attempt>()
    private val ipAttempts = ConcurrentHashMap<String, Attempt>()
    private val maxAttempts = 5
    private val maxIpAttempts = 20
    private val blockDurationMillis = TimeUnit.MINUTES.toMillis(10)

    fun checkAndIncrease(key: String, ip: String? = null) {
        val now = System.currentTimeMillis()
        // Kullanıcı bazlı kontrol
        val attempt = attempts.compute(key) { _, old ->
            val prev = old ?: Attempt(0, now, 0)
            if (prev.blockedUntil > now) {
                prev.copy()
            } else if (prev.lastAttempt + blockDurationMillis < now) {
                Attempt(1, now, 0)
            } else {
                val newCount = prev.count + 1
                if (newCount > maxAttempts) {
                    Attempt(newCount, now, now + blockDurationMillis)
                } else {
                    Attempt(newCount, now, 0)
                }
            }
        }!!
        if (attempt.blockedUntil > now) {
            throw RateLimitException("Too many attempts for user. Try again later.")
        }
        // IP bazlı kontrol
        if (ip != null) {
            val ipAttempt = ipAttempts.compute(ip) { _, old ->
                val prev = old ?: Attempt(0, now, 0)
                if (prev.blockedUntil > now) {
                    prev.copy()
                } else if (prev.lastAttempt + blockDurationMillis < now) {
                    Attempt(1, now, 0)
                } else {
                    val newCount = prev.count + 1
                    if (newCount > maxIpAttempts) {
                        Attempt(newCount, now, now + blockDurationMillis)
                    } else {
                        Attempt(newCount, now, 0)
                    }
                }
            }!!
            if (ipAttempt.blockedUntil > now) {
                throw RateLimitException("Too many attempts from IP. Try again later.")
            }
        }
    }

    fun isRateLimitExceeded(key: String, limit: Int, periodSeconds: Int): Boolean {
        val now = System.currentTimeMillis()
        val periodMillis = periodSeconds * 1000L
        val attempt = attempts[key]
        return if (attempt == null) {
            false
        } else {
            if (attempt.lastAttempt + periodMillis < now) {
                false
            } else {
                attempt.count >= limit
            }
        }
    }

    data class Attempt(val count: Int, val lastAttempt: Long, val blockedUntil: Long)
}

class RateLimitException(message: String) : RuntimeException(message)
