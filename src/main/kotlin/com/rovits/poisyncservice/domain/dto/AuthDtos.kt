package com.rovits.poisyncservice.domain.dto

import jakarta.validation.constraints.*

// ===== REQUEST MODELS =====

/**
 * Login request with email/password validation
 */
data class LoginRequest(
    @field:NotBlank(message = "error.validation.required")
    @field:Email(message = "error.validation.email")
    val email: String,

    @field:NotBlank(message = "error.validation.required")
    val password: String
)

/**
 * Social login request (Firebase token)
 */
data class SocialLoginRequest(
    @field:NotBlank(message = "error.validation.required")
    val firebaseToken: String,

    @field:NotBlank(message = "error.validation.required")
    @field:Pattern(
        regexp = "^(google|facebook|apple)$",
        message = "error.validation.provider.invalid"
    )
    val provider: String
)

/**
 * Registration request with comprehensive validation
 */
data class RegisterRequest(
    @field:NotBlank(message = "error.validation.required")
    @field:Size(min = 2, max = 100, message = "error.validation.name.size")
    val name: String,

    @field:NotBlank(message = "error.validation.required")
    @field:Email(message = "error.validation.email")
    val email: String,

    @field:NotBlank(message = "error.validation.required")
    @field:Size(min = 8, message = "error.validation.password.min")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "error.validation.password.strength"
    )
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