package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.client.GooglePlacesClient
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.util.ResponseHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/places")
@Tag(name = "Places", description = "Mekan Arama ve Detay İşlemleri")
class PlacesController(
    private val googlePlacesClient: GooglePlacesClient
) {
    private val logger = LoggerFactory.getLogger(PlacesController::class.java)

    @Operation(summary = "Yakındaki Mekanları Ara", description = "Belirtilen koordinat çevresindeki belirli tipteki mekanları listeler.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Arama başarılı"),
            SwaggerApiResponse(responseCode = "503", description = "Google API servisine ulaşılamadı")
        ]
    )
    @GetMapping("/nearby")
    fun searchNearby(
        @Parameter(description = "Enlem (Latitude)", example = "40.7128")
        @RequestParam lat: Double,

        @Parameter(description = "Boylam (Longitude)", example = "-74.0060")
        @RequestParam lng: Double,

        @Parameter(description = "Yarıçap (metre)", example = "5000.0")
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,

        @Parameter(description = "Mekan Tipi", example = "restaurant")
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<ApiResponse<SearchNearbyResponse>> = runBlocking {
        logger.info("Nearby search request: lat={}, lng={}, radius={}, type={}", lat, lng, radius, type)
        val response = googlePlacesClient.searchNearby(lat, lng, radius, type)
        return@runBlocking ResponseHelper.ok(response)
    }

    @Operation(summary = "Metin ile Mekan Ara", description = "Serbest metin (query) kullanarak mekan araması yapar.")
    @GetMapping("/text-search")
    fun searchText(
        @Parameter(description = "Aranacak metin (örn: Sushi)", example = "Sushi")
        @RequestParam query: String,

        @Parameter(description = "Dil kodu", example = "tr")
        @RequestParam(required = false, defaultValue = "tr") languageCode: String,

        @Parameter(description = "Maksimum sonuç sayısı", example = "20")
        @RequestParam(required = false, defaultValue = "20") maxResults: Int,

        @Parameter(description = "Konum önceliği için enlem")
        @RequestParam(required = false) lat: Double?,

        @Parameter(description = "Konum önceliği için boylam")
        @RequestParam(required = false) lng: Double?,

        @Parameter(description = "Konum önceliği için yarıçap")
        @RequestParam(required = false) radius: Double?
    ): ResponseEntity<ApiResponse<SearchTextResponse>> = runBlocking {
        logger.info("Text search request: query='{}', language={}", query, languageCode)

        val locationBias = if (lat != null && lng != null && radius != null) {
            LocationBias(
                circle = Circle(
                    center = Center(latitude = lat, longitude = lng),
                    radius = radius
                )
            )
        } else null

        val response = googlePlacesClient.searchText(
            textQuery = query,
            languageCode = languageCode,
            maxResultCount = maxResults,
            locationBias = locationBias
        )
        return@runBlocking ResponseHelper.ok(response)
    }

    @Operation(summary = "Mekan Detayı Getir", description = "Place ID'ye göre mekanın detaylı bilgilerini (adres, çalışma saatleri vb.) getirir.")
    @GetMapping("/details/{placeId}")
    fun getPlaceDetails(
        @Parameter(description = "Google Place ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
        @PathVariable placeId: String
    ): ResponseEntity<ApiResponse<PlaceDetails>> = runBlocking {
        logger.info("Place details request: placeId={}", placeId)
        val details = googlePlacesClient.getPlaceDetails(placeId)
        return@runBlocking ResponseHelper.ok(details)
    }
}