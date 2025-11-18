package com.rovits.poisyncservice.client

import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ExternalServiceException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class GooglePlacesClient(
    @Qualifier("googlePlacesWebClient") private val webClient: WebClient,
    @Value("\${google.api.key}") private val apiKey: String,
    private val cacheManager: CacheManager
) {
    private val logger = LoggerFactory.getLogger(GooglePlacesClient::class.java)

    private val searchFieldMask = "places.id,places.displayName"
    private val textSearchFieldMask = "places.id,places.displayName,places.formattedAddress"
    private val detailFieldMask = "id,displayName.text,formattedAddress,regularOpeningHours"

    suspend fun searchNearby(
        lat: Double,
        lng: Double,
        radius: Double,
        type: String
    ): SearchNearbyResponse {
        logger.info("Searching nearby places: lat={}, lng={}, radius={}m, type={}", lat, lng, radius, type)

        val requestBody = SearchNearbyRequest(
            includedTypes = listOf(type),
            locationRestriction = LocationRestriction(
                circle = Circle(
                    center = Center(latitude = lat, longitude = lng),
                    radius = radius
                )
            )
        )

        return try {
            val response = webClient.post()
                .uri("/places:searchNearby")
                .bodyValue(requestBody)
                .header("X-Goog-FieldMask", searchFieldMask)
                .retrieve()
                .awaitBody<SearchNearbyResponse>()

            logger.info("Found {} places", response.places?.size ?: 0)
            response
        } catch (e: Exception) {
            logger.error("Failed to search nearby places", e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.GOOGLE_API_ERROR,
                messageKey = "error.google.api.failed",
                serviceName = "Google Places API",
                cause = e
            )
        }
    }

    suspend fun searchText(
        textQuery: String,
        languageCode: String? = "tr",
        maxResultCount: Int? = 20,
        locationBias: LocationBias? = null
    ): SearchTextResponse {
        logger.info("Searching text: query='{}', language={}", textQuery, languageCode)

        val requestBody = SearchTextRequest(
            textQuery = textQuery,
            languageCode = languageCode,
            maxResultCount = maxResultCount,
            locationBias = locationBias
        )

        return try {
            val response = webClient.post()
                .uri("/places:searchText")
                .bodyValue(requestBody)
                .header("X-Goog-FieldMask", textSearchFieldMask)
                .retrieve()
                .awaitBody<SearchTextResponse>()

            logger.info("Text search found {} places", response.places?.size ?: 0)
            response
        } catch (e: Exception) {
            logger.error("Failed to search text", e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.GOOGLE_API_ERROR,
                messageKey = "error.google.api.failed",
                serviceName = "Google Places API",
                cause = e
            )
        }
    }

    suspend fun getPlaceDetails(placeId: String): PlaceDetails {
        logger.debug("Fetching place details: placeId={}", placeId)

        // Manual cache check
        val cache = cacheManager.getCache("placeDetails")
        val cachedValue = cache?.get(placeId)?.get() as? PlaceDetails

        if (cachedValue != null) {
            logger.debug("Cache hit for placeId={}", placeId)
            return cachedValue
        }

        logger.debug("Cache miss for placeId={}, calling API", placeId)

        return try {
            val details = webClient.get()
                .uri("/places/{placeId}", placeId)
                .header("X-Goog-FieldMask", detailFieldMask)
                .retrieve()
                .awaitBody<PlaceDetails>()

            cache?.put(placeId, details)
            logger.debug("Place details cached: placeId={}", placeId)

            details
        } catch (e: Exception) {
            logger.error("Failed to fetch place details for placeId={}", placeId, e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.GOOGLE_API_ERROR,
                messageKey = "error.google.api.failed",
                serviceName = "Google Places API",
                cause = e
            )
        }
    }
}