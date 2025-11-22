package com.rovits.poisyncservice.repository

import com.rovits.poisyncservice.domain.document.UserDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : MongoRepository<UserDocument, String> {

    fun findByEmail(email: String): Optional<UserDocument>

    fun findByFirebaseUid(firebaseUid: String): Optional<UserDocument>
}