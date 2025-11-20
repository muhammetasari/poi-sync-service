package com.rovits.poisyncservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.document.PoiDocument
import com.rovits.poisyncservice.domain.document.PoiOpeningHours
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.repository.PoiRepository
import com.rovits.poisyncservice.util.MessageKeys
import com.rovits.poisyncservice.util.MessageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

@Service
class PoiService(
    private val googleClient: GooglePlacesClient,
    private val poiRepository: PoiRepository,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val messageResolver: MessageResolver
) {
    private val logger = LoggerFactory.getLogger(PoiService::class.java)

    companion object {
        private const val REDIS_TTL_MINUTES = 10L
        private const val REDIS_DETAILS_TTL_HOURS = 24L
    }

    suspend fun searchNearby(lat: Double, lng: Double, radius: Double, type: String): SearchNearbyResponse {
        val cacheKey = "search:nearby:${round(lat)}:${round(lng)}:$radius:$type"
        val currentLang = messageResolver.getCurrentLocale().language

        getCached<SearchNearbyResponse>(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            val distance = Distance(radius / 1000.0, Metrics.KILOMETERS)

            val localPois = poiRepository.findByLocationNearAndType(Point(lng, lat), distance, type)

            if (localPois.isNotEmpty()) {
                logger.info("Source: MongoDB (Count: ${localPois.size})")
                val response = mapToNearbyResponse(localPois, currentLang)
                cacheToRedis(cacheKey, response)
                return@withContext response
            }

            logger.info("Source: Google API")
            val googleResponse = googleClient.searchNearby(lat, lng, radius, type)

            googleResponse.places?.let { savePlacesAsync(it, type) }
            cacheToRedis(cacheKey, googleResponse)
            return@withContext googleResponse
        }
    }

    suspend fun searchText(query: String, lang: String, max: Int, bias: LocationBias?): SearchTextResponse {
        val cacheKey = "search:text:${query.lowercase().trim()}:$lang"

        getCached<SearchTextResponse>(cacheKey)?.let { return it }

        logger.info("Source: Google API (Text)")
        val response = googleClient.searchText(query, lang, max, bias)

        response.places?.let { places ->
            val docs = places.map { it.toDocument() } // Text search sonucunda tip garantisi olmadığı için type=null olabilir
            try {
                poiRepository.saveAll(docs)
            } catch (e: Exception) {
                logger.warn("Failed to save text search results", e)
            }
        }

        cacheToRedis(cacheKey, response)
        return response
    }

    suspend fun getPlaceDetails(placeId: String): PlaceDetails {
        val cacheKey = "details:$placeId"
        val currentLang = messageResolver.getCurrentLocale().language

        getCached<PlaceDetails>(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            val localPoi = poiRepository.findByPlaceId(placeId)

            if (localPoi.isPresent) {
                logger.info("Source: MongoDB (Details)")
                val doc = localPoi.get()

                val details = PlaceDetails(
                    id = doc.placeId,
                    displayName = DisplayName(doc.name, currentLang),
                    formattedAddress = doc.address,
                    location = doc.location?.let { Location(it.y, it.x) },
                    openingHours = doc.openingHours?.let {
                        OpeningHours(it.openNow, it.weekdayDescriptions)
                    }
                )
                cacheToRedis(cacheKey, details, REDIS_DETAILS_TTL_HOURS, TimeUnit.HOURS)
                return@withContext details
            }

            logger.info("Source: Google API (Details)")
            val details = googleClient.getPlaceDetails(placeId)

            poiRepository.save(details.toDocument())
            cacheToRedis(cacheKey, details, REDIS_DETAILS_TTL_HOURS, TimeUnit.HOURS)
            return@withContext details
        }
    }

    // ================= HELPER METHODS =================

    private inline fun <reified T> getCached(key: String): T? {
        return try {
            val json = redisTemplate.opsForValue().get(key) ?: return null
            logger.info("Source: Redis Cache hit ($key)")
            objectMapper.readValue(json, T::class.java)
        } catch (e: Exception) {
            logger.error("Redis cache read error for key: $key", e)
            null
        }
    }

    private fun cacheToRedis(key: String, data: Any, ttl: Long = REDIS_TTL_MINUTES, unit: TimeUnit = TimeUnit.MINUTES) {
        try {
            val json = objectMapper.writeValueAsString(data)
            redisTemplate.opsForValue().set(key, json, ttl, unit)
        } catch (e: Exception) {
            logger.error("Redis cache write error", e)
        }
    }

    private fun savePlacesAsync(places: List<NearbyPlace>, type: String) {
        val docs = places.map { it.toDocument(type) }
        try {
            poiRepository.saveAll(docs)
        } catch (e: Exception) {
            logger.warn("Failed to save places to DB", e)
        }
    }

    private fun round(value: Double): Double {
        return value.toBigDecimal().setScale(4, RoundingMode.HALF_UP).toDouble()
    }

    // ================= MAPPERS =================

    private fun getUnknownName() = messageResolver.resolve(MessageKeys.POI_UNKNOWN_NAME)
    private fun getUnknownAddress() = messageResolver.resolve(MessageKeys.POI_UNKNOWN_ADDRESS)

    private fun NearbyPlace.toDocument(type: String? = null) = PoiDocument(
        placeId = this.id,
        name = this.displayName?.text ?: getUnknownName(),
        address = getUnknownAddress(),
        type = type,
        location = this.location?.let { GeoJsonPoint(it.longitude, it.latitude) },
        openingHours = null
    )

    private fun TextSearchPlace.toDocument() = PoiDocument(
        placeId = this.id,
        name = this.displayName?.text ?: getUnknownName(),
        address = this.formattedAddress ?: getUnknownAddress(),
        type = null,
        location = this.location?.let { GeoJsonPoint(it.longitude, it.latitude) },
        openingHours = null
    )

    private fun PlaceDetails.toDocument() = PoiDocument(
        placeId = this.id,
        name = this.displayName?.text ?: getUnknownName(),
        address = this.formattedAddress ?: getUnknownAddress(),
        type = null,
        location = this.location?.let { GeoJsonPoint(it.longitude, it.latitude) },
        openingHours = this.openingHours?.let {
            PoiOpeningHours(it.openNow, it.weekdayDescriptions)
        }
    )

    private fun mapToNearbyResponse(docs: List<PoiDocument>, currentLang: String): SearchNearbyResponse {
        val places = docs.map { doc ->
            NearbyPlace(
                id = doc.placeId,
                displayName = DisplayName(doc.name, currentLang),
                location = doc.location?.let { Location(it.y, it.x) }
            )
        }
        return SearchNearbyResponse(places)
    }
}