package com.rovits.poisyncservice.domain.dto

import com.rovits.poisyncservice.util.MessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

// ===== REQUEST MODELS =====

/**
 * Login request with email/password validation
 */
@Schema(description = "User login request payload")
data class LoginRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Email(message = MessageKeys.VALIDATION_EMAIL)
    @field:Schema(description = "User's email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    val email: String,

    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Schema(description = "User's password", example = "P@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
    val password: String
)

/**
 * Social login request (Firebase token)
 */
@Schema(description = "Social login request using Firebase token")
data class SocialLoginRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Schema(description = "Firebase ID Token received from client SDK", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc...", requiredMode = Schema.RequiredMode.REQUIRED)
    val firebaseToken: String,

    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Pattern(
        regexp = "^(google|facebook|apple)$",
        message = MessageKeys.VALIDATION_PROVIDER
    )
    @field:Schema(description = "Identity provider", example = "google", allowableValues = ["google", "facebook", "apple"], requiredMode = Schema.RequiredMode.REQUIRED)
    val provider: String
)

/**
 * Registration request with comprehensive validation
 */
@Schema(description = "New user registration request")
data class RegisterRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Size(min = 2, max = 100, message = MessageKeys.VALIDATION_NAME_SIZE)
    @field:Schema(description = "Full name of the user", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String,

    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Email(message = MessageKeys.VALIDATION_EMAIL)
    @field:Schema(description = "Valid email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    val email: String,

    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Size(min = 8, message = MessageKeys.VALIDATION_PASSWORD_MIN)
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = MessageKeys.VALIDATION_PASSWORD_STRENGTH
    )
    @field:Schema(description = "Strong password (min 8 chars, 1 upper, 1 lower, 1 digit)", example = "StrongP@ss1", requiredMode = Schema.RequiredMode.REQUIRED)
    val password: String
)

// ===== RESPONSE MODELS =====

@Schema(description = "Authentication response containing tokens and user info")
data class AuthResponse(
    @field:Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val token: String,

    @field:Schema(description = "Refresh Token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String?,

    @field:Schema(description = "Authenticated user details")
    val user: UserDto
)

@Schema(description = "Public user information")
data class UserDto(
    @field:Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,

    @field:Schema(description = "User email", example = "user@example.com")
    val email: String,

    @field:Schema(description = "User full name", example = "John Doe")
    val name: String?
)

@Schema(description = "Logout request payload")
data class LogoutRequest(
    @field:Schema(description = "Refresh token to be invalidated", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String?
)