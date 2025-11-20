package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.PoiService // YENİ
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
@Tag(name = "Places", description = "Mekan Arama ve Detay İşlemleri (Hybrid)")
class PlacesController(
    private val poiService: PoiService
) {
    private val logger = LoggerFactory.getLogger(PlacesController::class.java)

    @Operation(summary = "Yakındaki Mekanları Ara", description = "Redis -> MongoDB -> Google akışını kullanır.")
    @GetMapping("/nearby")
    fun searchNearby(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,
        @RequestParam(required = false, defaultValue = "restaurant") type: String
    ): ResponseEntity<ApiResponse<SearchNearbyResponse>> = runBlocking {
        val response = poiService.searchNearby(lat, lng, radius, type)
        return@runBlocking ResponseHelper.ok(response)
    }

    @Operation(summary = "Metin ile Ara", description = "Redis -> Google -> DB Save akışını kullanır.")
    @GetMapping("/text-search")
    fun searchText(
        @RequestParam query: String,
        @RequestParam(required = false, defaultValue = "tr") languageCode: String,
        @RequestParam(required = false, defaultValue = "20") maxResults: Int,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(required = false) radius: Double?
    ): ResponseEntity<ApiResponse<SearchTextResponse>> = runBlocking {
        val bias = if (lat != null && lng != null && radius != null) {
            LocationBias(Circle(Center(lat, lng), radius))
        } else null

        val response = poiService.searchText(query, languageCode, maxResults, bias)
        return@runBlocking ResponseHelper.ok(response)
    }

    @Operation(summary = "Mekan Detayı", description = "Redis -> MongoDB -> Google akışını kullanır.")
    @GetMapping("/details/{placeId}")
    fun getPlaceDetails(@PathVariable placeId: String): ResponseEntity<ApiResponse<PlaceDetails>> = runBlocking {
        val response = poiService.getPlaceDetails(placeId)
        return@runBlocking ResponseHelper.ok(response)
    }
}