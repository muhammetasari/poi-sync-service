package com.rovits.poisyncservice.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "users") // MongoDB collection adı: "users"
data class UserDocument(
    @Id
    val id: String = UUID.randomUUID().toString(), // Benzersiz ID

    @Indexed(unique = true) // Email'i benzersiz yap ve indexle (hızlı arama için)
    val email: String,

    val name: String?,

    // E-posta/şifre girişi için (Google girişinde null olabilir)
    val password: String?,

    // Kullanıcının nasıl kayıt olduğu (google, email vb.)
    val provider: String
)