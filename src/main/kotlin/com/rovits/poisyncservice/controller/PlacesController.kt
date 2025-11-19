package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.util.ResponseHelper
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
    ): ResponseEntity<ApiResponse<SearchNearbyResponse>> = runBlocking {
        logger.info("Nearby search request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)
        val response = googlePlacesClient.searchNearby(lat, lng, radius, type)
        return@runBlocking ResponseHelper.ok(response)
    }

    @GetMapping("/text-search")
    fun searchText(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "tr") languageCode: String,
        @RequestParam(required = false, defaultValue = "20") maxResults: Int,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(required = false) radius: Double?
    ): ResponseEntity<ApiResponse<SearchTextResponse>> = runBlocking {
        logger.info("Text search request: query='{}', language={}", query, languageCode)

        val locationBias = if (lat != null && lng != null && radius != null) {
            LocationBias(
                circle = Circle(
                    center = Center(latitude = lat, longitude = lng),
                    radius = radius
                )
            )
        } else null

        val response = googlePlacesClient.searchText(
            textQuery = query,
            languageCode = languageCode,
            maxResultCount = maxResults,
            locationBias = locationBias
        )
        return@runBlocking ResponseHelper.ok(response)
    }

    @GetMapping("/details/{placeId}")
    fun getPlaceDetails(
        @PathVariable placeId: String
    ): ResponseEntity<ApiResponse<PlaceDetails>> = runBlocking {
        logger.info("Place details request: placeId={}", placeId)
        val details = googlePlacesClient.getPlaceDetails(placeId)
        return@runBlocking ResponseHelper.ok(details)
    }
}