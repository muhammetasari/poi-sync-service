package com.rovits.poisyncservice.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty

// ----- İSTEK (Request) Modelleri -----
// Android'den POST /auth/login ile gelecek body
data class LoginRequest(
    val email: String,
    val password: String
)

// Android'den POST /auth/social-login ile gelecek body
data class SocialLoginRequest(
    val firebaseToken: String,
    val provider: String // "google"
)

// ----- CEVAP (Response) Modelleri -----
// Android'e başarılı giriş sonrası döneceğimiz body
data class AuthResponse(
    val token: String,
    val refreshToken: String?,
    val user: UserDto
)

// AuthResponse içinde kullanılacak kullanıcı modeli
data class UserDto(
    val id: String,
    val email: String,
    val name: String?
)

// POST /auth/register ile gelecek body
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)