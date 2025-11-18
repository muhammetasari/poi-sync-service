package com.rovits.poisyncservice.repository

import com.rovits.poisyncservice.domain.document.PoiDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PoiRepository : MongoRepository<PoiDocument, String> {
    fun findByPlaceId(placeId: String): Optional<PoiDocument>
}