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

    /**
     * Nearby Search - Belirli bir konumun yakınındaki POI'leri arar
     *
     * @param lat Enlem (latitude)
     * @param lng Boylam (longitude)
     * @param radius Arama yarıçapı (metre, default: 5000)
     * @param type POI tipi (örn: restaurant, cafe, hotel)
     * @return Bulunan POI'lerin listesi
     */
    @GetMapping("/nearby")
    fun searchNearby(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<SearchNearbyResponse> = runBlocking {
        logger.info("📍 Nearby Search isteği alındı")
        logger.info("   Konum: ($lat, $lng)")
        logger.info("   Yarıçap: ${radius}m")
        logger.info("   Tip: $type")

        return@runBlocking try {
            val response = googlePlacesClient.searchNearby(lat, lng, radius, type)
            logger.info("✅ ${response.places?.size ?: 0} POI bulundu")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("❌ Nearby search hatası: ${e.message}", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Text Search - Metin tabanlı POI arama
     *
     * @param query Aranacak metin (örn: "istanbul'daki en iyi restoranlar")
     * @param languageCode Dil kodu (default: "tr")
     * @param maxResults Maksimum sonuç sayısı (default: 20)
     * @param lat (Opsiyonel) Location bias için enlem
     * @param lng (Opsiyonel) Location bias için boylam
     * @param radius (Opsiyonel) Location bias için yarıçap
     * @return Bulunan POI'lerin listesi
     */
    @GetMapping("/text-search")
    fun searchText(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "tr") languageCode: String,
        @RequestParam(required = false, defaultValue = "20") maxResults: Int,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(required = false) radius: Double?
    ): ResponseEntity<SearchTextResponse> = runBlocking {
        logger.info("🔎 Text Search isteği alındı")
        logger.info("   Sorgu: \"$query\"")
        logger.info("   Dil: $languageCode")
        logger.info("   Max Sonuç: $maxResults")

        // Location bias varsa oluştur
        val locationBias = if (lat != null && lng != null && radius != null) {
            logger.info("   Location Bias: ($lat, $lng) - ${radius}m")
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
            logger.info("✅ ${response.places?.size ?: 0} POI bulundu")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("❌ Text search hatası: ${e.message}", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Place Details - Belirli bir POI'nin detaylı bilgilerini getirir
     *
     * @param placeId Google Places API'den alınan place ID
     * @return POI'nin detaylı bilgileri (ad, adres, çalışma saatleri)
     */
    @GetMapping("/details/{placeId}")
    fun getPlaceDetails(
        @PathVariable placeId: String
    ): ResponseEntity<PlaceDetails> = runBlocking {
        logger.info("📋 Place Details isteği alındı")
        logger.info("   Place ID: $placeId")

        return@runBlocking try {
            val details = googlePlacesClient.getPlaceDetails(placeId)
            logger.info("✅ POI detayı başarıyla getirildi: ${details.displayName?.text}")
            ResponseEntity.ok(details)
        } catch (e: Exception) {
            logger.error("❌ Place details hatası: ${e.message}", e)
            ResponseEntity.internalServerError().build()
        }
    }
}