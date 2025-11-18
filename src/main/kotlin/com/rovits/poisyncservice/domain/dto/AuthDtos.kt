package com.rovits.poisyncservice.domain.dto

// ===== REQUEST MODELS =====
data class LoginRequest(
    val email: String,
    val password: String
)

data class SocialLoginRequest(
    val firebaseToken: String,
    val provider: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// ===== RESPONSE MODELS =====
data class AuthResponse(
    val token: String,
    val refreshToken: String?,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String?
)