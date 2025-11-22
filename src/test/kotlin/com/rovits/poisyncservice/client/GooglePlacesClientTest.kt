package com.rovits.poisyncservice.client

import com.rovits.poisyncservice.constants.ApiEndpoints
import com.rovits.poisyncservice.constants.CacheConstants
import com.rovits.poisyncservice.constants.HttpConstants
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ExternalServiceException
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody

class GooglePlacesClientTest {

    private lateinit var googlePlacesClient: GooglePlacesClient
    private lateinit var webClient: WebClient
    private lateinit var requestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    private lateinit var requestBodySpec: WebClient.RequestBodySpec
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var responseSpec: WebClient.ResponseSpec

    private val testApiKey = "test-api-key-12345"

    @BeforeEach
    fun setup() {
        // Mock WebClient ve ilgili spec'leri oluştur
        webClient = mockk()
        requestBodyUriSpec = mockk()
        requestHeadersUriSpec = mockk()
        requestBodySpec = mockk()
        requestHeadersSpec = mockk()
        responseSpec = mockk()

        // MockK için coroutine desteğini etkinleştir
        mockkStatic("org.springframework.web.reactive.function.client.WebClientExtensionsKt")

        googlePlacesClient = GooglePlacesClient(webClient, testApiKey)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // ===================================
    // searchNearby() Tests
    // ===================================

    @Test
    fun `searchNearby should return SearchNearbyResponse successfully`() = runBlocking {
        // Given
        val lat = 41.008234
        val lng = 28.978456
        val radius = 1000.0
        val type = "restaurant"

        val expectedResponse = SearchNearbyResponse(
            places = listOf(
                NearbyPlace(
                    id = "ChIJN1t_tDeuEmsRUsoyG83frY4",
                    displayName = DisplayName(text = "Test Restaurant", languageCode = "en"),
                    location = Location(latitude = lat, longitude = lng)
                )
            )
        )

        // Mock chain: webClient.post().uri().bodyValue().header().retrieve().awaitBody()
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_NEARBY) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, CacheConstants.FIELD_MASK_NEARBY_SEARCH) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchNearbyResponse>() } returns expectedResponse

        // When
        val result = googlePlacesClient.searchNearby(lat, lng, radius, type)

        // Then
        assertNotNull(result)
        assertEquals(1, result.places?.size)
        assertEquals("ChIJN1t_tDeuEmsRUsoyG83frY4", result.places?.get(0)?.id)
        assertEquals("Test Restaurant", result.places?.get(0)?.displayName?.text)

        // Verify method calls
        verify { webClient.post() }
        verify { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_NEARBY) }
        verify { requestBodySpec.bodyValue(any()) }
        verify { requestBodySpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, CacheConstants.FIELD_MASK_NEARBY_SEARCH) }
        verify { requestBodySpec.retrieve() }
        coVerify { responseSpec.awaitBody<SearchNearbyResponse>() }
    }

    @Test
    fun `searchNearby should send correct request body`() = runBlocking {
        // Given
        val lat = 41.008234
        val lng = 28.978456
        val radius = 1500.0
        val type = "cafe"

        val capturedBody = slot<SearchNearbyRequest>()
        val expectedResponse = SearchNearbyResponse(places = emptyList())

        // Mock chain
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_NEARBY) } returns requestBodySpec
        every { requestBodySpec.bodyValue(capture(capturedBody)) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchNearbyResponse>() } returns expectedResponse

        // When
        googlePlacesClient.searchNearby(lat, lng, radius, type)

        // Then - Request body doğrulaması
        assertTrue(capturedBody.isCaptured)
        val requestBody = capturedBody.captured
        assertEquals(listOf(type), requestBody.includedTypes)
        assertEquals(lat, requestBody.locationRestriction.circle.center.latitude)
        assertEquals(lng, requestBody.locationRestriction.circle.center.longitude)
        assertEquals(radius, requestBody.locationRestriction.circle.radius)
    }

    @Test
    fun `searchNearby should throw ExternalServiceException on WebClient error`() = runBlocking {
        // Given
        val lat = 41.008234
        val lng = 28.978456
        val radius = 1000.0
        val type = "restaurant"

        val webClientException = WebClientResponseException.create(
            500,
            "Internal Server Error",
            org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        // Mock chain - Exception fırlat
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchNearbyResponse>() } throws webClientException

        // When & Then
        val exception = assertThrows<ExternalServiceException> {
            googlePlacesClient.searchNearby(lat, lng, radius, type)
        }

        assertEquals(ErrorCodes.GOOGLE_API_ERROR, exception.errorCode)
        assertEquals("error.google.api.failed", exception.messageKey)
        assertEquals("Google Places API", exception.serviceName)
        assertNotNull(exception.cause)
    }

    // ===================================
    // searchText() Tests
    // ===================================

    @Test
    fun `searchText should return SearchTextResponse successfully`() = runBlocking {
        // Given
        val query = "Starbucks Istanbul"
        val lang = "tr"
        val maxResults = 10
        val locationBias = LocationBias(
            circle = Circle(
                center = Center(latitude = 41.0, longitude = 29.0),
                radius = 5000.0
            )
        )

        val expectedResponse = SearchTextResponse(
            places = listOf(
                TextSearchPlace(
                    id = "ChIJ123abc",
                    displayName = DisplayName(text = "Starbucks Taksim", languageCode = "tr"),
                    formattedAddress = "Istiklal Cad. No:123, Beyoglu, Istanbul",
                    location = Location(latitude = 41.036, longitude = 28.984)
                )
            )
        )

        // Mock chain
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_TEXT) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, CacheConstants.FIELD_MASK_TEXT_SEARCH) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchTextResponse>() } returns expectedResponse

        // When
        val result = googlePlacesClient.searchText(query, lang, maxResults, locationBias)

        // Then
        assertNotNull(result)
        assertEquals(1, result.places?.size)
        assertEquals("ChIJ123abc", result.places?.get(0)?.id)
        assertEquals("Starbucks Taksim", result.places?.get(0)?.displayName?.text)
        assertEquals("Istiklal Cad. No:123, Beyoglu, Istanbul", result.places?.get(0)?.formattedAddress)

        // Verify correct endpoint was called
        verify { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_TEXT) }
        verify { requestBodySpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, CacheConstants.FIELD_MASK_TEXT_SEARCH) }
    }

    @Test
    fun `searchText should send correct request parameters`() = runBlocking {
        // Given
        val query = "Pizza near me"
        val lang = "en"
        val maxResults = 20
        val locationBias: LocationBias? = null

        val capturedBody = slot<SearchTextRequest>()
        val expectedResponse = SearchTextResponse(places = emptyList())

        // Mock chain
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_TEXT) } returns requestBodySpec
        every { requestBodySpec.bodyValue(capture(capturedBody)) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchTextResponse>() } returns expectedResponse

        // When
        googlePlacesClient.searchText(query, lang, maxResults, locationBias)

        // Then - Request body doğrulaması
        assertTrue(capturedBody.isCaptured)
        val requestBody = capturedBody.captured
        assertEquals(query, requestBody.textQuery)
        assertEquals(lang, requestBody.languageCode)
        assertEquals(maxResults, requestBody.maxResultCount)
        assertNull(requestBody.locationBias)
    }

    @Test
    fun `searchText should handle empty results`() = runBlocking {
        // Given
        val query = "NonExistentPlace12345XYZ"
        val expectedResponse = SearchTextResponse(places = null)

        // Mock chain
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_TEXT) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchTextResponse>() } returns expectedResponse

        // When
        val result = googlePlacesClient.searchText(query, "en", 10, null)

        // Then
        assertNotNull(result)
        assertNull(result.places)
    }

    @Test
    fun `searchText should throw ExternalServiceException on API error`() = runBlocking {
        // Given
        val query = "Test Query"
        val exception = RuntimeException("Network timeout")

        // Mock chain - Exception fırlat
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchTextResponse>() } throws exception

        // When & Then
        val thrownException = assertThrows<ExternalServiceException> {
            googlePlacesClient.searchText(query, "en", 10, null)
        }

        assertEquals(ErrorCodes.GOOGLE_API_ERROR, thrownException.errorCode)
        assertEquals("error.google.api.failed", thrownException.messageKey)
        assertEquals(exception, thrownException.cause)
    }

    // ===================================
    // getPlaceDetails() Tests
    // ===================================

    @Test
    fun `getPlaceDetails should return PlaceDetails successfully`() = runBlocking {
        // Given
        val placeId = "ChIJN1t_tDeuEmsRUsoyG83frY4"
        val expectedDetails = PlaceDetails(
            id = placeId,
            displayName = DisplayName(text = "Galata Tower", languageCode = "en"),
            formattedAddress = "Bereketzade, Galata Kulesi, 34421 Beyoglu/Istanbul",
            location = Location(latitude = 41.0256, longitude = 28.9744),
            openingHours = OpeningHours(
                openNow = true,
                weekdayDescriptions = listOf(
                    "Monday: 9:00 AM – 7:00 PM",
                    "Tuesday: 9:00 AM – 7:00 PM"
                )
            )
        )

        // Mock chain for GET request
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId) } returns requestHeadersSpec
        every { requestHeadersSpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, CacheConstants.FIELD_MASK_DETAILS) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } returns expectedDetails

        // When
        val result = googlePlacesClient.getPlaceDetails(placeId)

        // Then
        assertNotNull(result)
        assertEquals(placeId, result.id)
        assertEquals("Galata Tower", result.displayName?.text)
        assertEquals("Bereketzade, Galata Kulesi, 34421 Beyoglu/Istanbul", result.formattedAddress)
        assertNotNull(result.location)
        assertEquals(41.0256, result.location?.latitude)
        assertEquals(28.9744, result.location?.longitude)
        assertTrue(result.openingHours?.openNow == true)
        assertEquals(2, result.openingHours?.weekdayDescriptions?.size)

        // Verify GET request was used (not POST)
        verify { webClient.get() }
        verify { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId) }
        verify { requestHeadersSpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, CacheConstants.FIELD_MASK_DETAILS) }
        verify { requestHeadersSpec.retrieve() }
        coVerify { responseSpec.awaitBody<PlaceDetails>() }
    }

    @Test
    fun `getPlaceDetails should use correct field mask`() = runBlocking {
        // Given
        val placeId = "ChIJ_test_place_id"
        val expectedDetails = PlaceDetails(
            id = placeId,
            displayName = null,
            formattedAddress = null,
            location = null,
            openingHours = null
        )

        val capturedFieldMask = slot<String>()

        // Mock chain
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId) } returns requestHeadersSpec
        every { requestHeadersSpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, capture(capturedFieldMask)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } returns expectedDetails

        // When
        googlePlacesClient.getPlaceDetails(placeId)

        // Then - Field mask doğrulaması
        assertTrue(capturedFieldMask.isCaptured)
        assertEquals(CacheConstants.FIELD_MASK_DETAILS, capturedFieldMask.captured)
        assertEquals("id,displayName.text,formattedAddress,regularOpeningHours,location", capturedFieldMask.captured)
    }

    @Test
    fun `getPlaceDetails should handle null optional fields`() = runBlocking {
        // Given
        val placeId = "ChIJ_minimal_place"
        val minimalDetails = PlaceDetails(
            id = placeId,
            displayName = null,
            formattedAddress = null,
            location = null,
            openingHours = null
        )

        // Mock chain
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId) } returns requestHeadersSpec
        every { requestHeadersSpec.header(any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } returns minimalDetails

        // When
        val result = googlePlacesClient.getPlaceDetails(placeId)

        // Then
        assertNotNull(result)
        assertEquals(placeId, result.id)
        assertNull(result.displayName)
        assertNull(result.formattedAddress)
        assertNull(result.location)
        assertNull(result.openingHours)
    }

    @Test
    fun `getPlaceDetails should throw ExternalServiceException when place not found`() = runBlocking {
        // Given
        val placeId = "InvalidPlaceId123"
        val notFoundException = WebClientResponseException.create(
            404,
            "Not Found",
            org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        // Mock chain - 404 Exception
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId) } returns requestHeadersSpec
        every { requestHeadersSpec.header(any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } throws notFoundException

        // When & Then
        val exception = assertThrows<ExternalServiceException> {
            googlePlacesClient.getPlaceDetails(placeId)
        }

        assertEquals(ErrorCodes.GOOGLE_API_ERROR, exception.errorCode)
        assertEquals("error.google.api.failed", exception.messageKey)
        assertEquals("Google Places API", exception.serviceName)
        assertNotNull(exception.cause)
    }

    @Test
    fun `getPlaceDetails should throw ExternalServiceException on server error`() = runBlocking {
        // Given
        val placeId = "ChIJ_test"
        val serverException = WebClientResponseException.create(
            503,
            "Service Unavailable",
            org.springframework.http.HttpHeaders.EMPTY,
            ByteArray(0),
            null
        )

        // Mock chain
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, placeId) } returns requestHeadersSpec
        every { requestHeadersSpec.header(any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } throws serverException

        // When & Then
        val exception = assertThrows<ExternalServiceException> {
            googlePlacesClient.getPlaceDetails(placeId)
        }

        assertEquals(ErrorCodes.GOOGLE_API_ERROR, exception.errorCode)
        assertEquals(serverException, exception.cause)
    }

    // ===================================
    // Field Mask Tests
    // ===================================

    @Test
    fun `all methods should use correct field masks`() = runBlocking {
        // Given
        val nearbyFieldMaskSlot = slot<String>()
        val textFieldMaskSlot = slot<String>()
        val detailsFieldMaskSlot = slot<String>()

        // Mock for searchNearby
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_NEARBY) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, capture(nearbyFieldMaskSlot)) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchNearbyResponse>() } returns SearchNearbyResponse(emptyList())

        // When - searchNearby
        googlePlacesClient.searchNearby(41.0, 28.0, 1000.0, "restaurant")

        // Then - Nearby field mask
        assertEquals(CacheConstants.FIELD_MASK_NEARBY_SEARCH, nearbyFieldMaskSlot.captured)

        // Mock for searchText
        clearMocks(requestBodyUriSpec, requestBodySpec)
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_SEARCH_TEXT) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, capture(textFieldMaskSlot)) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchTextResponse>() } returns SearchTextResponse(emptyList())

        // When - searchText
        googlePlacesClient.searchText("test", "en", 10, null)

        // Then - Text search field mask
        assertEquals(CacheConstants.FIELD_MASK_TEXT_SEARCH, textFieldMaskSlot.captured)

        // Mock for getPlaceDetails
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(ApiEndpoints.GOOGLE_PLACES_DETAILS, any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.header(HttpConstants.HEADER_X_GOOG_FIELD_MASK, capture(detailsFieldMaskSlot)) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } returns PlaceDetails("test", null, null, null, null)

        // When - getPlaceDetails
        googlePlacesClient.getPlaceDetails("ChIJ_test")

        // Then - Details field mask
        assertEquals(CacheConstants.FIELD_MASK_DETAILS, detailsFieldMaskSlot.captured)
    }

    // ===================================
    // Exception Handling Tests
    // ===================================

    @Test
    fun `all methods should wrap any Exception in ExternalServiceException`() = runBlocking {
        val genericException = IllegalStateException("Unexpected error")

        // Test searchNearby
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<SearchNearbyResponse>() } throws genericException

        val nearbyException = assertThrows<ExternalServiceException> {
            googlePlacesClient.searchNearby(41.0, 28.0, 1000.0, "restaurant")
        }
        assertEquals(genericException, nearbyException.cause)
        assertEquals(ErrorCodes.GOOGLE_API_ERROR, nearbyException.errorCode)

        // Test searchText
        coEvery { responseSpec.awaitBody<SearchTextResponse>() } throws genericException
        val textException = assertThrows<ExternalServiceException> {
            googlePlacesClient.searchText("test", "en", 10, null)
        }
        assertEquals(genericException, textException.cause)

        // Test getPlaceDetails
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.header(any(), any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.awaitBody<PlaceDetails>() } throws genericException

        val detailsException = assertThrows<ExternalServiceException> {
            googlePlacesClient.getPlaceDetails("ChIJ_test")
        }
        assertEquals(genericException, detailsException.cause)
    }
}

