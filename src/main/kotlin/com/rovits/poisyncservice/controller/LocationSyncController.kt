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

@RestController // REST API controller olduğunu belirt
@RequestMapping("/api/sync") // Tüm endpoint'ler /api/sync ile başlar
class LocationSyncController(
    private val syncService: LocationSyncService // Service'i inject et
) {
    private val logger = LoggerFactory.getLogger(LocationSyncController::class.java)

    private val controllerScope = CoroutineScope(Dispatchers.IO) // Arka plan işleri için coroutine scope

    @PostMapping("/locations") // POST /api/sync/locations endpoint'i
    fun startLocationSync(
        @RequestParam lat: Double, // Zorunlu parametre: enlem
        @RequestParam lng: Double, // Zorunlu parametre: boylam
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double, // Opsiyonel: yarıçap (default: 5000m)
        @RequestParam(required = false, defaultValue = "restaurant") type: String // Opsiyonel: POI tipi (default: restaurant)
    ): ResponseEntity<String> {
        logger.info("📥 API İsteği alındı - POST /api/sync/locations")
        logger.info("📍 Parametreler: lat=$lat, lng=$lng, radius=$radius, type=$type")

        controllerScope.launch { // Arka planda async olarak çalıştır
            try {
                logger.info("⏳ Senkronizasyon servisi çağrılıyor...")
                syncService.syncPois(lat, lng, radius, type) // Sync işlemini başlat
                logger.info("✅ Senkronizasyon başarıyla tamamlandı")
            } catch (e: Exception) {
                logger.error("❌ Senkronizasyon hatası: ${e.message}", e) // Hata durumunda log bas
            }
        }

        logger.info("📤 HTTP 202 Accepted response gönderiliyor")
        return ResponseEntity.accepted().body("Senkronizasyon başlatıldı.") // Hemen 202 Accepted response döndür
    }
}