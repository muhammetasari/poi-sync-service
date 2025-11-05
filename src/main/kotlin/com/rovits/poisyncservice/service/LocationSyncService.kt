package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.document.PoiDocument
import com.rovits.poisyncservice.domain.document.PoiOpeningHours
import com.rovits.poisyncservice.repository.PoiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service // Spring service component'i
class LocationSyncService(
    private val apiClient: GooglePlacesClient, // Google Places client'ı inject et
    private val poiRepository: PoiRepository // MongoDB repository'yi inject et
) {
    private val logger = LoggerFactory.getLogger(LocationSyncService::class.java)

    suspend fun syncPois(lat: Double, lng: Double, radius: Double, type: String) { // Ana sync fonksiyonu (suspend = async)
        logger.info("🚀 Senkronizasyon başlatıldı - Lokasyon: ($lat, $lng), Yarıçap: ${radius}m, Tip: $type")

        withContext(Dispatchers.IO) { // IO thread'inde çalıştır (network/db işlemleri için)

            // 1. Yakındaki POI'leri ara
            logger.info("🔍 ADIM 1: Yakındaki POI'ler aranıyor...")
            val nearbyPlaces = apiClient.searchNearby(lat, lng, radius, type).places ?: emptyList()

            if (nearbyPlaces.isEmpty()) {
                logger.warn("⚠️ Hiç POI bulunamadı, senkronizasyon sonlandırılıyor")
                return@withContext
            }

            logger.info("📋 ${nearbyPlaces.size} POI bulundu, detaylar çekiliyor...")

            // 2. Her POI için detayları paralel olarak çek
            logger.info("🔄 ADIM 2: POI detayları paralel olarak çekiliyor...")
            val detailedPlaces = coroutineScope {
                nearbyPlaces.map { place ->
                    async { // Her birini paralel async task olarak başlat
                        try {
                            apiClient.getPlaceDetails(place.id) // POI detaylarını getir (cache'ten veya API'den)
                        } catch (e: Exception) {
                            logger.warn("⚠️ POI detay çekilemedi (${place.id}): ${e.message}")
                            null // Hata durumunda null döndür
                        }
                    }
                }
            }

            // 3. Başarılı sonuçları topla (null olanları filtrele)
            val successfulDetails = detailedPlaces.mapNotNull { it.await() }
            logger.info("✅ ${successfulDetails.size}/${nearbyPlaces.size} POI detayı başarıyla çekildi")

            var newCount = 0 // Yeni eklenen kayıt sayısı
            var updatedCount = 0 // Güncellenen kayıt sayısı
            var skippedCount = 0 // Değişmediği için atlanan kayıt sayısı

            // 4. Her POI için upsert işlemi yap
            logger.info("💾 ADIM 3: MongoDB'ye kayıt ediliyor...")
            successfulDetails.forEach { details ->
                // DTO'dan MongoDB document'ine dönüştür
                val newDoc = PoiDocument(
                    placeId = details.id, // Google'ın place ID'si
                    name = details.displayName?.text ?: "İsimsiz Yer", // POI adı
                    address = details.formattedAddress ?: "Adres Yok", // Adres
                    openingHours = details.openingHours?.let { // Çalışma saatleri (varsa)
                        PoiOpeningHours(
                            openNow = it.openNow, // Şu anda açık mı?
                            weekdayDescriptions = it.weekdayDescriptions // Haftalık çalışma saatleri
                        )
                    }
                )

                val existing = poiRepository.findByPlaceId(details.id) // DB'de var mı kontrol et

                if (existing.isPresent) { // Varsa
                    val existingDoc = existing.get()
                    if (hasChanged(existingDoc, newDoc)) { // Değişmişse
                        poiRepository.save(newDoc) // Güncelle
                        updatedCount++
                        logger.debug("🔄 Güncellendi: ${newDoc.name}")
                    } else {
                        skippedCount++
                        logger.debug("⏭️ Değişmedi: ${newDoc.name}")
                    }
                } else { // Yoksa
                    poiRepository.save(newDoc) // Yeni kayıt ekle
                    newCount++
                    logger.debug("✨ Yeni eklendi: ${newDoc.name}")
                }
            }

            logger.info("📊 SONUÇ: ✨ Yeni: $newCount | 🔄 Güncellenen: $updatedCount | ⏭️ Değişmedi: $skippedCount")
        }
    }

    private fun hasChanged(existing: PoiDocument, new: PoiDocument): Boolean { // İçerik değişmiş mi kontrol et
        return existing.name != new.name || // Ad değişmiş mi?
                existing.address != new.address || // Adres değişmiş mi?
                existing.openingHours != new.openingHours // Çalışma saatleri değişmiş mi?
    }
}