package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.service.LocationSyncService
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
class LocationSyncController(
    private val syncService: LocationSyncService
) {
    private val logger = LoggerFactory.getLogger(LocationSyncController::class.java)
    private val controllerScope = CoroutineScope(Dispatchers.IO)

    @PostMapping("/locations")
    fun startLocationSync(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<String> {
        logger.info("Received sync request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)

        controllerScope.launch {
            try {
                syncService.syncPois(lat, lng, radius, type)
            } catch (e: Exception) {
                logger.error("Sync failed", e)
            }
        }

        return ResponseEntity.accepted().body("Synchronization started")
    }
}
