package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.LocationSyncService
import com.rovits.poisyncservice.util.ResponseHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
@Tag(name = "Location Sync", description = "POI Veri Senkronizasyonu Yönetimi")
class LocationSyncController(
    private val syncService: LocationSyncService
) {
    private val logger = LoggerFactory.getLogger(LocationSyncController::class.java)
    private val controllerScope = CoroutineScope(Dispatchers.IO)

    @Operation(
        summary = "Konum Bazlı Senkronizasyon Başlat",
        description = "Belirtilen koordinatlar ve yarıçap içerisindeki mekanları Google Places API'den çeker ve veritabanına kaydeder. İşlem asenkron olarak arka planda yürütülür."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "202", description = "Senkronizasyon işlemi başarıyla kuyruğa alındı/başlatıldı"),
            SwaggerApiResponse(responseCode = "400", description = "Geçersiz koordinat veya parametre hatası"),
            SwaggerApiResponse(responseCode = "401", description = "Yetkisiz erişim (API Key veya Token eksik)"),
            SwaggerApiResponse(responseCode = "500", description = "Sunucu içi hata")
        ]
    )
    @PostMapping("/locations")
    fun startLocationSync(
        @Parameter(description = "Merkez noktanın enlem değeri (Latitude)", example = "40.7128")
        @RequestParam lat: Double,

        @Parameter(description = "Merkez noktanın boylam değeri (Longitude)", example = "-74.0060")
        @RequestParam lng: Double,

        @Parameter(description = "Tarama yapılacak yarıçap (metre cinsinden)", example = "5000.0")
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,

        @Parameter(description = "Aranacak mekan tipi (örn: restaurant, cafe)", example = "restaurant")
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Received sync request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)

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