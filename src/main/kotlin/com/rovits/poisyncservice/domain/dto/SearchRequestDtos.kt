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
    val textQuery: String, // Aranacak metin (örn: "istanbul'daki en iyi restoranlar")
    val languageCode: String? = "tr", // Dil kodu (opsiyonel)
    val maxResultCount: Int? = 20, // Maksimum sonuç sayısı (opsiyonel)
    val locationBias: LocationBias? = null // Lokasyon bias (opsiyonel)
)

data class LocationBias(
    val circle: Circle
)