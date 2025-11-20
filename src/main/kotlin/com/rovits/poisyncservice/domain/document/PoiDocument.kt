package com.rovits.poisyncservice.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pois")
data class PoiDocument(
    @Id
    val placeId: String,

    // Metin araması için indeks ekliyoruz
    @Indexed
    val name: String,

    val address: String,

    // Coğrafi arama için 2dsphere indeksi
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    val location: GeoJsonPoint?,

    val openingHours: PoiOpeningHours?
)

data class PoiOpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)