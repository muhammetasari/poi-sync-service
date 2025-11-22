package com.rovits.poisyncservice.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "users")
data class UserDocument(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Indexed(unique = true)
    val firebaseUid: String,

    @Indexed(unique = true)
    val email: String,

    val name: String?,

    val authProvider: String, // "password", "google.com", "facebook.com", "apple.com"

    val role: String = "user", // "user" or "admin"

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)