package com.rovits.poisyncservice.repository

import com.rovits.poisyncservice.domain.document.PoiDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository // Spring repository component'i
interface PoiRepository : MongoRepository<PoiDocument, String> { // MongoDB CRUD işlemleri için (PoiDocument tipi, String ID)
    fun findByPlaceId(placeId: String): Optional<PoiDocument> // placeId'ye göre POI bul (upsert için kullanılır)
}
