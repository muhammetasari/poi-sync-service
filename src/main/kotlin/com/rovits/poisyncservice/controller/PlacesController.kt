package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.dto.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/places")
class PlacesController(
    private val googlePlacesClient: GooglePlacesClient
) {
    private val logger = LoggerFactory.getLogger(PlacesController::class.java)

    @GetMapping("/nearby")
    fun searchNearby(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<SearchNearbyResponse> = runBlocking {
        logger.info("Nearby search request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)

        return@runBlocking try {
            val response = googlePlacesClient.searchNearby(lat, lng, radius, type)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Nearby search failed", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/text-search")
    fun searchText(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "tr") languageCode: String,
        @RequestParam(required = false, defaultValue = "20") maxResults: Int,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(required = false) radius: Double?
    ): ResponseEntity<SearchTextResponse> = runBlocking {
        logger.info("Text search request: query='{}', language={}", query, languageCode)

        val locationBias = if (lat != null && lng != null && radius != null) {
            LocationBias(
                circle = Circle(
                    center = Center(latitude = lat, longitude = lng),
                    radius = radius
                )
            )
        } else null

        return@runBlocking try {
            val response = googlePlacesClient.searchText(
                textQuery = query,
                languageCode = languageCode,
                maxResultCount = maxResults,
                locationBias = locationBias
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Text search failed", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/details/{placeId}")
    fun getPlaceDetails(
        @PathVariable placeId: String
    ): ResponseEntity<PlaceDetails> = runBlocking {
        logger.info("Place details request: placeId={}", placeId)

        return@runBlocking try {
            val details = googlePlacesClient.getPlaceDetails(placeId)
            ResponseEntity.ok(details)
        } catch (e: Exception) {
            logger.error("Failed to fetch place details", e)
            ResponseEntity.internalServerError().build()
        }
    }
}
