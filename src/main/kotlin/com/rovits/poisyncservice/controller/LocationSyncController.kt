package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.LocationSyncService
import com.rovits.poisyncservice.util.ResponseHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Location Sync API", description = "Management endpoints for triggering POI data synchronization from Google Maps to local database.")
class LocationSyncController(
    private val syncService: LocationSyncService
) {
    private val logger = LoggerFactory.getLogger(LocationSyncController::class.java)
    private val controllerScope = CoroutineScope(Dispatchers.IO)

    @Operation(
        summary = "Start Location Sync",
        description = "Triggers an asynchronous background process to fetch places from Google API within the given radius and upsert them into the local MongoDB. This endpoint returns immediately with 'Accepted' status."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "202",
                description = "Synchronization successfully started/queued",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Validation failed (e.g., invalid coordinates or radius)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Unauthorized (Missing API Key or Token)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Forbidden (Requires Admin Role)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/locations")
    fun startLocationSync(
        @Parameter(description = "Latitude of the center point", example = "40.7128", required = true)
        @RequestParam lat: Double,

        @Parameter(description = "Longitude of the center point", example = "-74.0060", required = true)
        @RequestParam lng: Double,

        @Parameter(description = "Radius to scan in meters (must be > 0)", example = "5000.0", required = false)
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,

        @Parameter(description = "POI type to filter (e.g., restaurant, gym)", example = "restaurant", required = false)
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Received sync request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)

        // Synchronous validation before async execution
        syncService.validateRequest(lat, lng, radius)

        controllerScope.launch {
            try {
                syncService.syncPois(lat, lng, radius, type)
            } catch (e: Exception) {
                logger.error("Sync failed", e)
            }
        }

        return ResponseHelper.accepted("Synchronization started")
    }
}