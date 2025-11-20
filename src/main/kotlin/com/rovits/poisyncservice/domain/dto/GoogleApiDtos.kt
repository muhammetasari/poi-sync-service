package com.rovits.poisyncservice.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

// ===== SEARCH NEARBY =====
@Schema(description = "Response for nearby search operation")
data class SearchNearbyResponse(
    @field:Schema(description = "List of found places")
    val places: List<NearbyPlace>?
)

@Schema(description = "Simplified place object for nearby search")
data class NearbyPlace(
    @field:Schema(description = "Google Place ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
    val id: String,

    @field:Schema(description = "Display name of the place")
    val displayName: DisplayName?,

    @field:Schema(description = "Geographic location")
    val location: Location?
)

// ===== TEXT SEARCH =====
@Schema(description = "Response for text search operation")
data class SearchTextResponse(
    @field:Schema(description = "List of places matching the query")
    val places: List<TextSearchPlace>?
)

@Schema(description = "Place object for text search results")
data class TextSearchPlace(
    @field:Schema(description = "Google Place ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
    val id: String,

    @field:Schema(description = "Display name")
    val displayName: DisplayName?,

    @field:Schema(description = "Formatted address", example = "123 Main St, New York, NY 10001")
    val formattedAddress: String?,

    @field:Schema(description = "Geographic location")
    val location: Location?
)

// ===== PLACE DETAILS =====
@Schema(description = "Detailed information about a place")
data class PlaceDetails(
    @field:Schema(description = "Google Place ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
    val id: String,

    @field:Schema(description = "Display name")
    val displayName: DisplayName?,

    @field:Schema(description = "Full address", example = "Istiklal Cd. No:10, Beyoglu, Istanbul")
    val formattedAddress: String?,

    @field:Schema(description = "Geographic location")
    val location: Location?,

    @JsonProperty("regularOpeningHours")
    @field:Schema(description = "Opening hours information")
    val openingHours: OpeningHours?
)

// ===== SHARED MODELS =====
data class DisplayName(
    @field:Schema(description = "Text content", example = "Starbucks")
    val text: String?,
    @field:Schema(description = "Language code", example = "en")
    val languageCode: String?
)

data class OpeningHours(
    @field:Schema(description = "Is currently open", example = "true")
    val openNow: Boolean?,
    @field:Schema(description = "Weekly opening descriptions", example = "[\"Monday: 9:00 AM – 5:00 PM\"]")
    val weekdayDescriptions: List<String>?
)

data class Location(
    @field:Schema(description = "Latitude", example = "41.0082")
    val latitude: Double,
    @field:Schema(description = "Longitude", example = "28.9784")
    val longitude: Double
)