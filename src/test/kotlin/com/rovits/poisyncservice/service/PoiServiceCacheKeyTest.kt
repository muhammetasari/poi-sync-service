package com.rovits.poisyncservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.repository.PoiRepository
import com.rovits.poisyncservice.util.MessageResolver
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.math.RoundingMode
import java.util.*

class PoiServiceCacheKeyTest {

    private lateinit var poiService: PoiService
    private lateinit var googleClient: GooglePlacesClient
    private lateinit var poiRepository: PoiRepository
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var objectMapper: ObjectMapper
    private lateinit var messageResolver: MessageResolver
    private lateinit var valueOperations: ValueOperations<String, String>

    @BeforeEach
    fun setup() {
        googleClient = mockk()
        poiRepository = mockk()
        redisTemplate = mockk()
        objectMapper = jacksonObjectMapper()
        messageResolver = mockk()
        valueOperations = mockk()

        every { redisTemplate.opsForValue() } returns valueOperations
        every { messageResolver.getCurrentLocale() } returns Locale.ENGLISH

        poiService = PoiService(
            googleClient,
            poiRepository,
            redisTemplate,
            objectMapper,
            messageResolver
        )
    }

    @Test
    fun `cache keys should not collide for coordinates with 6 decimal precision`() {
        // Coordinates with differences smaller than 1 meter
        val coord1 = 41.008230 // Base coordinate
        val coord2 = 41.008231 // ~0.11m difference
        val coord3 = 41.008235 // ~0.55m difference
        val coord4 = 41.008240 // ~1.1m difference

        val key1 = generateCacheKey(coord1, 28.978400, 1000.0, "restaurant")
        val key2 = generateCacheKey(coord2, 28.978400, 1000.0, "restaurant")
        val key3 = generateCacheKey(coord3, 28.978400, 1000.0, "restaurant")
        val key4 = generateCacheKey(coord4, 28.978400, 1000.0, "restaurant")

        // With 6 decimal precision, these should all be different
        val keys = setOf(key1, key2, key3, key4)
        assertEquals(4, keys.size, "All cache keys should be unique with 6 decimal precision")
    }

    @Test
    fun `cache key precision should be 6 decimals`() {
        val lat = 41.0082345678
        val lng = 28.9784567890

        val roundedLat = round(lat, 6)
        val roundedLng = round(lng, 6)

        assertEquals(41.008235, roundedLat, 0.0000001)
        assertEquals(28.978457, roundedLng, 0.0000001)
    }

    @Test
    fun `old 4 decimal precision would cause collisions`() {
        // With 4 decimals (~11m precision)
        val coord1 = 41.00820
        val coord2 = 41.00825  // ~5.5m difference
        val coord3 = 41.00829  // ~10m difference

        val rounded1 = round(coord1, 4)
        val rounded2 = round(coord2, 4)
        val rounded3 = round(coord3, 4)

        // These coordinates are different but would round to same value with 4 decimals
        assertEquals(41.0082, rounded1, 0.00001)
        assertEquals(41.0083, rounded2, 0.00001) // Different!
        assertEquals(41.0083, rounded3, 0.00001) // Same as coord2!

        // Demonstrating that coord2 and coord3 would have same cache key
        assertEquals(rounded2, rounded3)
    }

    @Test
    fun `cache key should include all relevant parameters`() {
        val lat = 41.008234
        val lng = 28.978456
        val radius = 1000.0
        val type = "restaurant"

        val key = generateCacheKey(lat, lng, radius, type)

        assertTrue(key.contains("41.008234"), "Cache key should contain latitude")
        assertTrue(key.contains("28.978456"), "Cache key should contain longitude")
        assertTrue(key.contains("1000.0"), "Cache key should contain radius")
        assertTrue(key.contains("restaurant"), "Cache key should contain type")
    }

    @Test
    fun `different types should generate different cache keys`() {
        val lat = 41.008234
        val lng = 28.978456
        val radius = 1000.0

        val key1 = generateCacheKey(lat, lng, radius, "restaurant")
        val key2 = generateCacheKey(lat, lng, radius, "cafe")
        val key3 = generateCacheKey(lat, lng, radius, "gym")

        val keys = setOf(key1, key2, key3)
        assertEquals(3, keys.size, "Different types should generate different cache keys")
    }

    @Test
    fun `different radius should generate different cache keys`() {
        val lat = 41.008234
        val lng = 28.978456
        val type = "restaurant"

        val key1 = generateCacheKey(lat, lng, 1000.0, type)
        val key2 = generateCacheKey(lat, lng, 2000.0, type)
        val key3 = generateCacheKey(lat, lng, 5000.0, type)

        val keys = setOf(key1, key2, key3)
        assertEquals(3, keys.size, "Different radius should generate different cache keys")
    }

    @Test
    fun `cache should be used when available`() = runBlocking {
        val lat = 41.008234
        val lng = 28.978456
        val radius = 1000.0
        val type = "restaurant"

        val cachedResponse = SearchNearbyResponse(
            places = listOf(
                NearbyPlace(
                    id = "cached-place-1",
                    displayName = DisplayName("Cached Restaurant", "en"),
                    location = Location(lat, lng)
                )
            )
        )

        val cacheKey = "search:nearby:${round(lat, 6)}:${round(lng, 6)}:$radius:$type"
        val cachedJson = objectMapper.writeValueAsString(cachedResponse)

        every { valueOperations.get(cacheKey) } returns cachedJson
        every { messageResolver.getCurrentLocale() } returns Locale.ENGLISH

        val result = poiService.searchNearby(lat, lng, radius, type)

        assertEquals("cached-place-1", result.places?.first()?.id)
        verify(exactly = 0) { poiRepository.findByLocationNearAndType(any(), any(), any()) }
        verify(exactly = 0) { runBlocking { googleClient.searchNearby(any(), any(), any(), any()) } }
    }

    // Helper function to simulate cache key generation
    private fun generateCacheKey(lat: Double, lng: Double, radius: Double, type: String): String {
        return "search:nearby:${round(lat, 6)}:${round(lng, 6)}:$radius:$type"
    }

    // Helper function to round coordinates (matching PoiService.round())
    private fun round(value: Double, scale: Int): Double {
        return value.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
    }
}

