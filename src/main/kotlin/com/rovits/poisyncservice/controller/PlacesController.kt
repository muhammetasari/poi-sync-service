package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.PoiService
import com.rovits.poisyncservice.util.ResponseHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/places")
@Tag(name = "Places API", description = "Operations for searching and retrieving Point of Interest (POI) details. Implements Hybrid Search (Redis -> MongoDB -> Google API).")
class PlacesController(
    private val poiService: PoiService
) {
    private val logger = LoggerFactory.getLogger(PlacesController::class.java)

    @Operation(
        summary = "Search Nearby Places",
        description = "Searches for places within a specified radius around a coordinate. Uses caching strategies to minimize external API calls."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Successfully retrieved nearby places",
                content = [Content(schema = Schema(implementation = SearchNearbyResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid parameters (lat/lng/radius)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized / Invalid API Key",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @GetMapping("/nearby")
    fun searchNearby(
        @Parameter(description = "Latitude of the center point", example = "41.0082", required = true)
        @RequestParam lat: Double,

        @Parameter(description = "Longitude of the center point", example = "28.9784", required = true)
        @RequestParam lng: Double,

        @Parameter(description = "Search radius in meters", example = "1000.0", required = false)
        @RequestParam(required = false, defaultValue = com.rovits.poisyncservice.constants.DefaultValues.DEFAULT_RADIUS_METERS.toString()) radius: Double,

        @Parameter(description = "Type of place to search for (e.g., restaurant, cafe)", example = "restaurant", required = false)
        @RequestParam(required = false, defaultValue = com.rovits.poisyncservice.constants.DefaultValues.DEFAULT_PLACE_TYPE) type: String
    ): ResponseEntity<ApiResponse<SearchNearbyResponse>> = runBlocking {
        val response = poiService.searchNearby(lat, lng, radius, type)
        return@runBlocking ResponseHelper.ok(response)
    }

    @Operation(
        summary = "Text Search",
        description = "Searches for places based on a text query. Supports optional location bias."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Search completed successfully",
                content = [Content(schema = Schema(implementation = SearchTextResponse::class))]
            )
        ]
    )
    @GetMapping("/text-search")
    fun searchText(
        @Parameter(description = "Text query to search", example = "Best sushi in Istanbul", required = true)
        @RequestParam query: String,

        @Parameter(description = "Language code for results", example = "tr", required = false)
        @RequestParam(required = false, defaultValue = com.rovits.poisyncservice.constants.DefaultValues.DEFAULT_LANGUAGE_CODE) languageCode: String,

        @Parameter(description = "Maximum number of results", example = "10", required = false)
        @RequestParam(required = false, defaultValue = com.rovits.poisyncservice.constants.DefaultValues.DEFAULT_MAX_RESULTS.toString()) maxResults: Int,

        @Parameter(description = "Optional latitude for location bias", example = "41.0082")
        @RequestParam(required = false) lat: Double?,

        @Parameter(description = "Optional longitude for location bias", example = "28.9784")
        @RequestParam(required = false) lng: Double?,

        @Parameter(description = "Optional radius for location bias", example = "5000.0")
        @RequestParam(required = false) radius: Double?
    ): ResponseEntity<ApiResponse<SearchTextResponse>> = runBlocking {
        val bias = if (lat != null && lng != null && radius != null) {
            LocationBias(Circle(Center(lat, lng), radius))
        } else null

        val response = poiService.searchText(query, languageCode, maxResults, bias)
        return@runBlocking ResponseHelper.ok(response)
    }

    @Operation(
        summary = "Get Place Details",
        description = "Retrieves detailed information about a specific place by its ID."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Details retrieved successfully",
                content = [Content(schema = Schema(implementation = PlaceDetails::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Place not found",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @GetMapping("/details/{placeId}")
    fun getPlaceDetails(
        @Parameter(description = "Unique Place ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4", required = true)
        @PathVariable placeId: String
    ): ResponseEntity<ApiResponse<PlaceDetails>> = runBlocking {
        val response = poiService.getPlaceDetails(placeId)
        return@runBlocking ResponseHelper.ok(response)
    }
}