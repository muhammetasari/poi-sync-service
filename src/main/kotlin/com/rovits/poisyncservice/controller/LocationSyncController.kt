package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.LocationSyncService
import com.rovits.poisyncservice.util.ResponseHelper
import com.rovits.poisyncservice.sync.JobStatusManager
import com.rovits.poisyncservice.sync.JobStatus
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.exception.ErrorCodes
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
        @RequestParam(required = false, defaultValue = com.rovits.poisyncservice.constants.DefaultValues.DEFAULT_RADIUS_METERS.toString()) radius: Double,

        @Parameter(description = "POI type to filter (e.g., restaurant, gym)", example = "restaurant", required = false)
        @RequestParam(required = false, defaultValue = com.rovits.poisyncservice.constants.DefaultValues.DEFAULT_PLACE_TYPE) type: String
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Received sync request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)

        // Synchronous validation before async execution
        syncService.validateRequest(lat, lng, radius)

        val jobId = JobStatusManager.createJob()
        logger.info("Sync job started: jobId={}, lat={}, lng={}, radius={}, type={}", jobId, lat, lng, radius, type)
        controllerScope.launch {
            try {
                syncService.syncPois(lat, lng, radius, type)
                logger.info("Sync job completed: jobId={}", jobId)
                JobStatusManager.setJobStatus(jobId, JobStatus.COMPLETED)
            } catch (e: Exception) {
                logger.error("Sync job failed: jobId={}, error={}", jobId, e.message, e)
                JobStatusManager.setJobStatus(jobId, JobStatus.FAILED, e.message)
            }
        }

        return ResponseHelper.accepted(jobId)
    }

    @Operation(
        summary = "Get Sync Job Status",
        description = "Returns the current status of a background location sync job by jobId."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Job status returned",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Job not found",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @GetMapping("/status/{jobId}")
    fun getJobStatus(
        @PathVariable jobId: String
    ): ResponseEntity<ApiResponse<Any>> {
        val (status, error) = JobStatusManager.getJobStatus(jobId)
        return if (status != null) {
            val result = mutableMapOf<String, Any?>("status" to status.name)
            if (status == JobStatus.FAILED && error != null) {
                result["error"] = error
            }
            ResponseHelper.ok(result)
        } else {
            val errorDetail = ErrorDetail.of(
                code = ErrorCodes.POI_NOT_FOUND,
                message = "Job not found"
            )
            ResponseHelper.errorAny(errorDetail, org.springframework.http.HttpStatus.NOT_FOUND)
        }
    }
}