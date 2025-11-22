package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.document.PoiDocument
import com.rovits.poisyncservice.domain.document.PoiOpeningHours
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ExternalServiceException
import com.rovits.poisyncservice.exception.ValidationException
import com.rovits.poisyncservice.repository.PoiRepository
import com.rovits.poisyncservice.util.MessageKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.stereotype.Service

@Service
class LocationSyncService(
    private val apiClient: GooglePlacesClient,
    private val poiRepository: PoiRepository
) {
    private val logger = LoggerFactory.getLogger(LocationSyncService::class.java)

    fun validateRequest(lat: Double, lng: Double, radius: Double) {
        validateCoordinates(lat, lng)
        validateRadius(radius)
    }

    suspend fun syncPois(lat: Double, lng: Double, radius: Double, type: String) {
        validateRequest(lat, lng, radius)

        val contextMap = MDC.getCopyOfContextMap()

        logger.info("Starting POI sync: lat={}, lng={}, radius={}m, type={}", lat, lng, radius, type)

        withContext(Dispatchers.IO) {
            // MDC Context'i bu thread'e geri yükle
            if (contextMap != null) MDC.setContextMap(contextMap)

            try {
                val nearbyPlaces = apiClient.searchNearby(lat, lng, radius, type).places ?: emptyList()

                if (nearbyPlaces.isEmpty()) {
                    logger.warn("No places found for given criteria. Sync stopping.")
                    return@withContext
                }

                logger.info("Found {} places, fetching details...", nearbyPlaces.size)

                // Step 2: Fetch details in parallel
                val detailedPlaces = coroutineScope {
                    nearbyPlaces.map { place ->
                        async {
                            if (contextMap != null) MDC.setContextMap(contextMap)
                            try {
                                logger.debug("Fetching details for placeId: {}", place.id)
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

                if (successfulDetails.isEmpty()) {
                    logger.error("All detail fetch requests failed! Check 'Failed to fetch details' logs above.")
                    return@withContext
                }

                logger.info("Successfully fetched {} details. Starting database save...", successfulDetails.size)

                // Step 4: Upsert to MongoDB
                var savedCount = 0
                successfulDetails.forEach { details ->
                    try {
                        val newDoc = PoiDocument(
                            placeId = details.id,
                            name = details.displayName?.text ?: "Unnamed Place",
                            address = details.formattedAddress ?: "No Address",
                            type = type,
                            location = details.location?.let { GeoJsonPoint(it.longitude, it.latitude) },
                            openingHours = details.openingHours?.let {
                                PoiOpeningHours(
                                    openNow = it.openNow,
                                    weekdayDescriptions = it.weekdayDescriptions
                                )
                            }
                        )

                        // Simply save - MongoDB will handle upsert based on placeId being the @Id
                        poiRepository.save(newDoc)
                        savedCount++
                    } catch (e: Exception) {
                        logger.error("Failed to save POI to DB: {}", details.id, e)
                    }
                }

                logger.info("Sync completed. Total saved/updated records: {}", savedCount)

            } catch (e: Exception) {
                logger.error("POI sync failed unexpectedly", e)
                throw ExternalServiceException(
                    errorCode = ErrorCodes.POI_SYNC_FAILED,
                    messageKey = MessageKeys.POI_SYNC_FAILED,
                    serviceName = "LocationSync",
                    cause = e
                )
            } finally {
                MDC.clear()
            }
        }
    }

    private fun validateCoordinates(lat: Double, lng: Double) {
        if (lat < -90.0 || lat > 90.0) {
            throw ValidationException(
                errorCode = ErrorCodes.INVALID_LATITUDE,
                messageKey = MessageKeys.VALIDATION_LATITUDE,
                messageArgs = arrayOf(lat),
                fieldName = "latitude"
            )
        }
        if (lng < -180.0 || lng > 180.0) {
            throw ValidationException(
                errorCode = ErrorCodes.INVALID_LONGITUDE,
                messageKey = MessageKeys.VALIDATION_LONGITUDE,
                messageArgs = arrayOf(lng),
                fieldName = "longitude"
            )
        }
    }

    private fun validateRadius(radius: Double) {
        if (radius <= 0 || radius > 50000) {
            throw ValidationException(
                errorCode = ErrorCodes.INVALID_RADIUS,
                messageKey = MessageKeys.VALIDATION_RADIUS,
                messageArgs = arrayOf(radius),
                fieldName = "radius"
            )
        }
    }
}