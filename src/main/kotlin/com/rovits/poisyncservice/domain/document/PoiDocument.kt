package com.rovits.poisyncservice.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pois")
data class PoiDocument(
    @Id
    val placeId: String,
    val name: String,
    val address: String,
    val openingHours: PoiOpeningHours?
)

data class PoiOpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)