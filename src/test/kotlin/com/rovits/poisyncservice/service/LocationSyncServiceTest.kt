package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.exception.ValidationException
import com.rovits.poisyncservice.repository.PoiRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LocationSyncServiceTest {

    @Mock
    private lateinit var googlePlacesClient: GooglePlacesClient

    @Mock
    private lateinit var poiRepository: PoiRepository

    private lateinit var locationSyncService: LocationSyncService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        locationSyncService = LocationSyncService(googlePlacesClient, poiRepository)
    }

    @Test
    fun `should throw ValidationException for invalid latitude`() {
        assertThrows<ValidationException> {
            runBlocking {
                locationSyncService.syncPois(91.0, 0.0, 1000.0, "restaurant")
            }
        }
    }

    @Test
    fun `should throw ValidationException for invalid longitude`() {
        assertThrows<ValidationException> {
            runBlocking {
                locationSyncService.syncPois(0.0, 181.0, 1000.0, "restaurant")
            }
        }
    }

    @Test
    fun `should throw ValidationException for invalid radius`() {
        assertThrows<ValidationException> {
            runBlocking {
                locationSyncService.syncPois(0.0, 0.0, -1.0, "restaurant")
            }
        }
    }
}

