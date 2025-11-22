package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.constants.DefaultValues
import com.rovits.poisyncservice.constants.SecurityConstants
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitService {
    private val attempts = ConcurrentHashMap<String, Attempt>()
    private val ipAttempts = ConcurrentHashMap<String, Attempt>()
    private val maxAttempts = DefaultValues.MAX_AUTH_ATTEMPTS
    private val maxIpAttempts = DefaultValues.MAX_IP_ATTEMPTS
    private val blockDurationMillis = DefaultValues.BLOCK_DURATION_MILLIS

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
            throw RateLimitException(SecurityConstants.ERROR_TOO_MANY_USER_ATTEMPTS)
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
                throw RateLimitException(SecurityConstants.ERROR_TOO_MANY_IP_ATTEMPTS)
            }
        }
    }

    fun isRateLimitExceeded(key: String, limit: Int, periodSeconds: Int): Boolean {
        val now = System.currentTimeMillis()
        val periodMillis = periodSeconds * 1000L

        val attempt = attempts.compute(key) { _, old ->
            val prev = old ?: Attempt(0, now, 0)
            if (prev.lastAttempt + periodMillis < now) {
                // Period expired, reset counter
                Attempt(1, now, 0)
            } else {
                // Increment counter within period
                Attempt(prev.count + 1, now, 0)
            }
        }!!

        return attempt.count > limit
    }

    data class Attempt(val count: Int, val lastAttempt: Long, val blockedUntil: Long)
}

class RateLimitException(message: String) : RuntimeException(message)
