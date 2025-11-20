package com.rovits.poisyncservice.repository

import com.rovits.poisyncservice.domain.document.PoiDocument
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PoiRepository : MongoRepository<PoiDocument, String> {
    fun findByPlaceId(placeId: String): Optional<PoiDocument>

    // Nearby Search için (Konum + Mesafe)
    fun findByLocationNear(location: Point, distance: Distance): List<PoiDocument>

    // Text Search için (İsim içinde arama - Case Insensitive)
    fun findByNameContainingIgnoreCase(name: String): List<PoiDocument>
}