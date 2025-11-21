package com.rovits.poisyncservice.client

import com.rovits.poisyncservice.constants.ApiEndpoints
import com.rovits.poisyncservice.constants.CacheConstants
import com.rovits.poisyncservice.constants.HttpConstants
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ExternalServiceException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class GooglePlacesClient(
    @Qualifier("googlePlacesWebClient") private val webClient: WebClient,
    @Value("\${google.api.key}") private val apiKey: String
) {
    private val logger = LoggerFactory.getLogger(GooglePlacesClient::class.java)

    private val searchFieldMask = CacheConstants.FIELD_MASK_NEARBY_SEARCH
    private val textSearchFieldMask = CacheConstants.FIELD_MASK_TEXT_SEARCH
    private val detailFieldMask = CacheConstants.FIELD_MASK_DETAILS

    suspend fun searchNearby(lat: Double, lng: Double, radius: Double, type: String): SearchNearbyResponse {
        logger.info("Google API Nearby: lat={}, lng={}", lat, lng)
        val requestBody = SearchNearbyRequest(
            includedTypes = listOf(type),
            locationRestriction = LocationRestriction(circle = Circle(center = Center(lat, lng), radius = radius))
        )
        return executeRequest(ApiEndpoints.GOOGLE_PLACES_SEARCH_NEARBY, requestBody, searchFieldMask)
    }

    suspend fun searchText(query: String, lang: String, max: Int, bias: LocationBias?): SearchTextResponse {
        logger.info("Google API Text: query={}", query)
        val requestBody = SearchTextRequest(
            textQuery = query, languageCode = lang, maxResultCount = max, locationBias = bias
        )
        return executeRequest(ApiEndpoints.GOOGLE_PLACES_SEARCH_TEXT, requestBody, textSearchFieldMask)
    }

    suspend fun getPlaceDetails(placeId: String): PlaceDetails {
        logger.debug("Google API Details: id={}", placeId)
        return try {
            webClient.get().uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId)
                .header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, detailFieldMask)
                .retrieve().awaitBody()
        } catch (e: Exception) {
            throw handleException(e)
        }
    }

    private suspend inline fun <reified T> executeRequest(uri: String, body: Any, mask: String): T {
        return try {
            webClient.post().uri(uri).bodyValue(body)
                .header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, mask)
                .retrieve().awaitBody()
        } catch (e: Exception) {
            throw handleException(e)
        }
    }

    private fun handleException(e: Exception): ExternalServiceException {
        logger.error("Google API Error", e)
        return ExternalServiceException(
            errorCode = ErrorCodes.GOOGLE_API_ERROR,
            messageKey = "error.google.api.failed",
            serviceName = "Google Places API",
            cause = e
        )
    }
}