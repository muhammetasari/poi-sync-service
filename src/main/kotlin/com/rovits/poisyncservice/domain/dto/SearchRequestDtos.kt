package com.rovits.poisyncservice.domain.dto

// ===== NEARBY SEARCH REQUEST =====
data class SearchNearbyRequest(
    val includedTypes: List<String>,
    val locationRestriction: LocationRestriction
)

data class LocationRestriction(
    val circle: Circle
)

data class Circle(
    val center: Center,
    val radius: Double
)

data class Center(
    val latitude: Double,
    val longitude: Double
)

// ===== TEXT SEARCH REQUEST =====
data class SearchTextRequest(
    val textQuery: String,
    val languageCode: String? = "tr",
    val maxResultCount: Int? = 20,
    val locationBias: LocationBias? = null
)

data class LocationBias(
    val circle: Circle
)