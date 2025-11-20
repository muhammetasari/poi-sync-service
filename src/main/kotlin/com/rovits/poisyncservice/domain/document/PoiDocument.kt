package com.rovits.poisyncservice.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pois")
@CompoundIndexes(
    // Konum ve Tip bazlı birleşik arama için indeks (Örn: "Yakınımdaki Restoranlar")
    CompoundIndex(name = "geo_type_idx", def = "{'location': '2dsphere', 'type': 1}")
)
data class PoiDocument(
    @Id
    val placeId: String,

    // Full-Text Search için Text Index (Ağırlıklı)
    // İsim eşleşmeleri, adres eşleşmelerinden daha puanlı olacak (weight: 2 vs 1)
    @TextIndexed(weight = 2.0f)
    val name: String,

    @TextIndexed(weight = 1.0f)
    val address: String,

    // Compound index için 'type' alanı eklendi
    val type: String? = null,

    // Tekil coğrafi aramalar için yedek indeks
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    val location: GeoJsonPoint?,

    val openingHours: PoiOpeningHours?
)

data class PoiOpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)