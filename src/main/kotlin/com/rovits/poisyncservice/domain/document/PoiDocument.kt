package com.rovits.poisyncservice.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pois") // MongoDB collection adı: "pois"
data class PoiDocument( // POI verilerini MongoDB'de saklayan model
    @Id // MongoDB'deki _id field'i
    val placeId: String, // Google Places API'den gelen place ID (unique identifier)

    val name: String, // POI adı (örn: "Starbucks")
    val address: String, // POI adresi

    val openingHours: PoiOpeningHours? // Çalışma saatleri (opsiyonel, null olabilir)
)

data class PoiOpeningHours( // POI çalışma saatleri alt-modeli
    val openNow: Boolean?, // Şu anda açık mı? (true/false/null)
    val weekdayDescriptions: List<String>? // Haftalık çalışma saatleri listesi (örn: ["Pazartesi: 09:00-18:00"])
)
