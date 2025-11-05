package com.rovits.poisyncservice.client

import com.rovits.poisyncservice.domain.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component // Spring component olarak işaretle
class GooglePlacesClient(
    @Qualifier("googlePlacesWebClient") private val webClient: WebClient, // Google Places için özel WebClient
    @Value("\${google.api.key}") private val apiKey: String, // API key'i application.properties'ten al
    private val cacheManager: CacheManager // Cache manager inject et
) {
    private val logger = LoggerFactory.getLogger(GooglePlacesClient::class.java)

    private val searchFieldMask = "places.id,places.displayName" // Search'te dönecek field'ler
    private val textSearchFieldMask = "places.id,places.displayName,places.formattedAddress" // Text search'te dönecek field'ler
    private val detailFieldMask = "id,displayName.text,formattedAddress,regularOpeningHours" // Detail'de dönecek field'ler

    suspend fun searchNearby( // Yakındaki POI'leri ara (suspend = async)
        lat: Double, // Enlem
        lng: Double, // Boylam
        radius: Double, // Yarıçap (metre)
        type: String // POI tipi (restaurant, cafe, vb.)
    ): SearchNearbyResponse {
        logger.info("🔍 POI arama başlatıldı - Konum: ($lat, $lng), Yarıçap: ${radius}m, Tip: $type")

        val requestBody = SearchNearbyRequest(
            includedTypes = listOf(type),
            locationRestriction = LocationRestriction(
                circle = Circle(
                    center = Center(
                        latitude = lat,
                        longitude = lng
                    ),
                    radius = radius
                )
            )
        )

        logger.debug("📤 Google Places API'ye istek gönderiliyor...")

        return try {
            val response = webClient.post() // POST isteği gönder
                .uri("/places:searchNearby") // Endpoint
                .bodyValue(requestBody) // Data class objesi gönder
                //.header("X-Goog-Api-Key", apiKey) // API key header (ÖNEMLİ!)
                .header("X-Goog-FieldMask", searchFieldMask) // Dönmesini istediğimiz field'ler
                .retrieve() // İsteği çalıştır
                .awaitBody<SearchNearbyResponse>() // Response'u bekle ve dönüştür

            val placeCount = response.places?.size ?: 0
            logger.info("✅ Arama tamamlandı - $placeCount POI bulundu")
            response
        } catch (e: Exception) {
            logger.error("❌ Google Places API hatası: ${e.message}", e)
            throw e
        }
    }

    suspend fun searchText( // Metin tabanlı POI arama (suspend = async)
        textQuery: String, // Aranacak metin
        languageCode: String? = "tr", // Dil kodu
        maxResultCount: Int? = 20, // Maksimum sonuç sayısı
        locationBias: LocationBias? = null // Lokasyon bias (opsiyonel)
    ): SearchTextResponse {
        logger.info("🔎 Text search başlatıldı - Sorgu: \"$textQuery\", Dil: $languageCode")

        val requestBody = SearchTextRequest(
            textQuery = textQuery,
            languageCode = languageCode,
            maxResultCount = maxResultCount,
            locationBias = locationBias
        )

        logger.debug("📤 Google Places API'ye text search isteği gönderiliyor...")

        return try {
            val response = webClient.post() // POST isteği gönder
                .uri("/places:searchText") // Endpoint
                .bodyValue(requestBody) // Data class objesi gönder
                 //.header("X-Goog-Api-Key", apiKey) // API key header
                .header("X-Goog-FieldMask", textSearchFieldMask) // Dönmesini istediğimiz field'ler
                .retrieve() // İsteği çalıştır
                .awaitBody<SearchTextResponse>() // Response'u bekle ve dönüştür

            val placeCount = response.places?.size ?: 0
            logger.info("✅ Text search tamamlandı - $placeCount POI bulundu")
            response
        } catch (e: Exception) {
            logger.error("❌ Text search hatası: ${e.message}", e)
            throw e
        }
    }

    suspend fun getPlaceDetails(placeId: String): PlaceDetails { // POI detaylarını getir
        logger.debug("📍 POI detayı çekiliyor - ID: $placeId")

        // Manuel cache kontrolü
        val cache = cacheManager.getCache("placeDetails")
        val cachedValue = cache?.get(placeId)?.get() as? PlaceDetails

        if (cachedValue != null) {
            logger.info("✅ 💾 CACHE HIT - [placeDetails] key: $placeId | Redis'ten alındı")
            return cachedValue
        }

        logger.info("❌ 🌐 CACHE MISS - [placeDetails] key: $placeId | Google API çağrılıyor...")

        return try {
            val startTime = System.currentTimeMillis()

            val details = webClient.get() // GET isteği gönder
                .uri("/places/{placeId}", placeId) // Endpoint ve path variable
                //.header("X-Goog-Api-Key", apiKey) // API key header
                .header("X-Goog-FieldMask", detailFieldMask) // Dönmesini istediğimiz field'ler
                .retrieve() // İsteği çalıştır
                .awaitBody<PlaceDetails>() // Response'u bekle ve dönüştür

            val duration = System.currentTimeMillis() - startTime

            // Cache'e kaydet
            cache?.put(placeId, details)
            logger.info("💾 Cache'e kaydedildi - [placeDetails] key: $placeId (~${duration}ms)")
            logger.debug("✅ POI detayı alındı - ${details.displayName?.text}")

            details
        } catch (e: Exception) {
            logger.error("❌ POI detay hatası (ID: $placeId): ${e.message}")
            throw e
        }
    }
}