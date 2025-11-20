package com.rovits.poisyncservice.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty

// ===== SEARCH NEARBY =====
data class SearchNearbyResponse(
    val places: List<NearbyPlace>?
)

data class NearbyPlace(
    val id: String,
    val displayName: DisplayName?,
    val location: Location? // YENİ
)

// ===== TEXT SEARCH =====
data class SearchTextResponse(
    val places: List<TextSearchPlace>?
)

data class TextSearchPlace(
    val id: String,
    val displayName: DisplayName?,
    val formattedAddress: String?,
    val location: Location? // YENİ
)

// ===== PLACE DETAILS =====
data class PlaceDetails(
    val id: String,
    val displayName: DisplayName?,
    val formattedAddress: String?,
    val location: Location?, // YENİ
    @JsonProperty("regularOpeningHours")
    val openingHours: OpeningHours?
)

// ===== SHARED MODELS =====
data class DisplayName(
    val text: String?,
    val languageCode: String?
)

data class OpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)

data class Location(
    val latitude: Double,
    val longitude: Double
)