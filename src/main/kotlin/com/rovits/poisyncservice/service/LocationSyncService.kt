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

@Service
class LocationSyncService(
    private val apiClient: GooglePlacesClient,
    private val poiRepository: PoiRepository
) {
    private val logger = LoggerFactory.getLogger(LocationSyncService::class.java)

    suspend fun syncPois(lat: Double, lng: Double, radius: Double, type: String) {
        logger.info("Starting POI sync: lat={}, lng={}, radius={}m, type={}", lat, lng, radius, type)

        withContext(Dispatchers.IO) {
            // Step 1: Search nearby places
            val nearbyPlaces = apiClient.searchNearby(lat, lng, radius, type).places ?: emptyList()

            if (nearbyPlaces.isEmpty()) {
                logger.warn("No places found for given criteria")
                return@withContext
            }

            logger.info("Found {} places, fetching details", nearbyPlaces.size)

            // Step 2: Fetch details in parallel
            val detailedPlaces = coroutineScope {
                nearbyPlaces.map { place ->
                    async {
                        try {
                            apiClient.getPlaceDetails(place.id)
                        } catch (e: Exception) {
                            logger.warn("Failed to fetch details for placeId={}: {}", place.id, e.message)
                            null
                        }
                    }
                }
            }

            // Step 3: Filter successful results
            val successfulDetails = detailedPlaces.mapNotNull { it.await() }
            logger.info("Successfully fetched {}/{} place details", successfulDetails.size, nearbyPlaces.size)

            var newCount = 0
            var updatedCount = 0
            var skippedCount = 0

            // Step 4: Upsert to MongoDB
            successfulDetails.forEach { details ->
                val newDoc = PoiDocument(
                    placeId = details.id,
                    name = details.displayName?.text ?: "Unnamed Place",
                    address = details.formattedAddress ?: "No Address",
                    openingHours = details.openingHours?.let {
                        PoiOpeningHours(
                            openNow = it.openNow,
                            weekdayDescriptions = it.weekdayDescriptions
                        )
                    }
                )

                val existing = poiRepository.findByPlaceId(details.id)

                if (existing.isPresent) {
                    val existingDoc = existing.get()
                    if (hasChanged(existingDoc, newDoc)) {
                        poiRepository.save(newDoc)
                        updatedCount++
                        logger.debug("Updated POI: {}", newDoc.name)
                    } else {
                        skippedCount++
                    }
                } else {
                    poiRepository.save(newDoc)
                    newCount++
                    logger.debug("Created new POI: {}", newDoc.name)
                }
            }

            logger.info("Sync completed: new={}, updated={}, skipped={}", newCount, updatedCount, skippedCount)
        }
    }

    private fun hasChanged(existing: PoiDocument, new: PoiDocument): Boolean {
        return existing.name != new.name ||
                existing.address != new.address ||
                existing.openingHours != new.openingHours
    }
}
