package com.rovits.poisyncservice.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RateLimitServiceTest {

    private lateinit var rateLimitService: RateLimitService

    @BeforeEach
    fun setup() {
        rateLimitService = RateLimitService()
    }

    @Test
    fun `should not exceed rate limit on first request`() {
        val key = "test-user"
        val limit = 5
        val period = 60

        val exceeded = rateLimitService.isRateLimitExceeded(key, limit, period)

        assertFalse(exceeded, "First request should not exceed rate limit")
    }

    @Test
    fun `should increment counter on each request`() {
        val key = "test-user"
        val limit = 5
        val period = 60

        // Make 5 requests (should all pass)
        repeat(5) {
            val exceeded = rateLimitService.isRateLimitExceeded(key, limit, period)
            assertFalse(exceeded, "Request ${it + 1} should not exceed limit of $limit")
        }

        // 6th request should exceed limit
        val exceeded = rateLimitService.isRateLimitExceeded(key, limit, period)
        assertTrue(exceeded, "Request 6 should exceed limit of $limit")
    }

    @Test
    fun `should reset counter after period expires`() {
        val key = "test-user"
        val limit = 3
        val period = 1 // 1 second

        // Make 3 requests
        repeat(3) {
            rateLimitService.isRateLimitExceeded(key, limit, period)
        }

        // 4th request should exceed
        assertTrue(rateLimitService.isRateLimitExceeded(key, limit, period))

        // Wait for period to expire
        Thread.sleep(1100)

        // Next request should reset counter and pass
        val exceeded = rateLimitService.isRateLimitExceeded(key, limit, period)
        assertFalse(exceeded, "Counter should reset after period expires")
    }

    @Test
    fun `should handle multiple keys independently`() {
        val key1 = "user1"
        val key2 = "user2"
        val limit = 3
        val period = 60

        // Make 3 requests for user1
        repeat(3) {
            rateLimitService.isRateLimitExceeded(key1, limit, period)
        }

        // user1 should exceed
        assertTrue(rateLimitService.isRateLimitExceeded(key1, limit, period))

        // user2 should not exceed (independent counter)
        assertFalse(rateLimitService.isRateLimitExceeded(key2, limit, period))
    }

    @Test
    fun `checkAndIncrease should throw exception when user rate limit exceeded`() {
        val email = "test@example.com"

        // Exceed user rate limit
        repeat(6) {
            try {
                rateLimitService.checkAndIncrease(email)
            } catch (e: RateLimitException) {
                // Expected on 6th attempt
            }
        }

        // Should throw on next attempt
        assertThrows<RateLimitException> {
            rateLimitService.checkAndIncrease(email)
        }
    }

    @Test
    fun `checkAndIncrease should throw exception when IP rate limit exceeded`() {
        val email = "test@example.com"
        val ip = "192.168.1.1"

        // Exceed IP rate limit
        repeat(21) {
            try {
                rateLimitService.checkAndIncrease(email, ip)
            } catch (e: RateLimitException) {
                // Expected on 21st attempt
            }
        }

        // Should throw on next attempt
        assertThrows<RateLimitException> {
            rateLimitService.checkAndIncrease(email, ip)
        }
    }

    @Test
    fun `should be thread-safe under concurrent access`() {
        val key = "concurrent-user"
        val limit = 100
        val period = 60
        val threads = 10
        val requestsPerThread = 20

        val results = mutableListOf<Boolean>()
        val threadList = (1..threads).map {
            Thread {
                repeat(requestsPerThread) {
                    val exceeded = rateLimitService.isRateLimitExceeded(key, limit, period)
                    synchronized(results) {
                        results.add(exceeded)
                    }
                }
            }
        }

        threadList.forEach { it.start() }
        threadList.forEach { it.join() }

        // Total requests = 10 * 20 = 200
        // First 100 should pass, rest should fail
        val passedCount = results.count { !it }
        val failedCount = results.count { it }

        assertTrue(passedCount <= limit, "Should respect rate limit under concurrent access")
        assertTrue(failedCount > 0, "Some requests should be blocked")
    }
}

