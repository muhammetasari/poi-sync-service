package com.rovits.poisyncservice.repository

import com.rovits.poisyncservice.domain.document.PoiDocument
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PoiRepository : MongoRepository<PoiDocument, String> {
    fun findByPlaceId(placeId: String): Optional<PoiDocument>

    // Nearby Search: Konum + Mesafe
    // (type filtresi eklenirse, geo_type_idx compound index'ini kullanır)
    fun findByLocationNear(location: Point, distance: Distance): List<PoiDocument>

    // Nearby Search + Type Filter (Compound Index Kullanır)
    fun findByLocationNearAndType(location: Point, distance: Distance, type: String): List<PoiDocument>

    // Full-Text Search (Text Index Kullanır)
    // Regex yerine native text search performansı sağlar
    @Query("{'\$text': {'\$search': ?0}}")
    fun searchByText(query: String): List<PoiDocument>
}