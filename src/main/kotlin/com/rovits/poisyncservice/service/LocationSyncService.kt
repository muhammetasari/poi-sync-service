package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.document.PoiDocument
import com.rovits.poisyncservice.domain.document.PoiOpeningHours
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ExternalServiceException
import com.rovits.poisyncservice.exception.ValidationException
import com.rovits.poisyncservice.repository.PoiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.stereotype.Service

@Service
class LocationSyncService(
    private val apiClient: GooglePlacesClient,
    private val poiRepository: PoiRepository
) {
    private val logger = LoggerFactory.getLogger(LocationSyncService::class.java)

    suspend fun syncPois(lat: Double, lng: Double, radius: Double, type: String) {
        // Validate input parameters
        validateCoordinates(lat, lng)
        validateRadius(radius)

        logger.info("Starting POI sync: lat={}, lng={}, radius={}m, type={}", lat, lng, radius, type)

        withContext(Dispatchers.IO) {
            try {
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
                        location = details.location?.let { GeoJsonPoint(it.longitude, it.latitude) },
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
            } catch (e: Exception) {
                logger.error("POI sync failed", e)
                throw ExternalServiceException(
                    errorCode = ErrorCodes.POI_SYNC_FAILED,
                    messageKey = "error.poi.sync.failed",
                    serviceName = "LocationSync",
                    cause = e
                )
            }
        }
    }

    private fun validateCoordinates(lat: Double, lng: Double) {
        if (lat < -90 || lat > 90) {
            throw ValidationException(
                errorCode = ErrorCodes.INVALID_LATITUDE,
                messageKey = "error.validation.latitude",
                messageArgs = arrayOf(lat),
                fieldName = "latitude"
            )
        }

        if (lng < -180 || lng > 180) {
            throw ValidationException(
                errorCode = ErrorCodes.INVALID_LONGITUDE,
                messageKey = "error.validation.longitude",
                messageArgs = arrayOf(lng),
                fieldName = "longitude"
            )
        }
    }

    private fun validateRadius(radius: Double) {
        if (radius <= 0) {
            throw ValidationException(
                errorCode = ErrorCodes.INVALID_RADIUS,
                messageKey = "error.validation.radius",
                messageArgs = arrayOf(radius),
                fieldName = "radius"
            )
        }
    }

    private fun hasChanged(existing: PoiDocument, new: PoiDocument): Boolean {
        return existing.name != new.name ||
                existing.address != new.address ||
                existing.openingHours != new.openingHours ||
                existing.location != new.location ||
                existing.placeId != new.placeId
    }
}