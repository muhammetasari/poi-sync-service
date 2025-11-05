package com.rovits.poisyncservice.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty

// ===== SEARCH NEARBY =====
// Google Places API - Search Nearby endpoint'inin response modeli
data class SearchNearbyResponse(
    val places: List<NearbyPlace>? // Bulunan POI'lerin listesi
)

// Search sonucundaki her bir POI
data class NearbyPlace(
    val id: String, // Place ID (unique identifier)
    val displayName: DisplayName? // POI adı
)

// ===== TEXT SEARCH =====
// Google Places API - Text Search endpoint'inin response modeli
data class SearchTextResponse(
    val places: List<TextSearchPlace>? // Bulunan POI'lerin listesi
)

// Text search sonucundaki her bir POI
data class TextSearchPlace(
    val id: String, // Place ID (unique identifier)
    val displayName: DisplayName?, // POI adı
    val formattedAddress: String? // Adres (text search'te formattedAddress da dönebilir)
)

// ===== PLACE DETAILS =====
// Google Places API - Place Details endpoint'inin response modeli
data class PlaceDetails(
    val id: String, // Place ID
    val displayName: DisplayName?, // POI adı
    val formattedAddress: String?, // Formatlanmış adres
    @JsonProperty("regularOpeningHours") // API'den "regularOpeningHours" olarak geliyor, kodda "openingHours" olarak kullanıyoruz
    val openingHours: OpeningHours? // Çalışma saatleri
)

// ===== SHARED MODELS =====
// POI adı modeli (çoklu dil desteği için)
data class DisplayName(
    val text: String?, // Görünen ad (örn: "Starbucks")
    val languageCode: String? // Dil kodu (örn: "tr", "en")
)

// Çalışma saatleri modeli
data class OpeningHours(
    val openNow: Boolean?, // Şu anda açık mı?
    val weekdayDescriptions: List<String>? // Haftalık çalışma saatleri (örn: ["Pazartesi: 09:00-18:00"])
)