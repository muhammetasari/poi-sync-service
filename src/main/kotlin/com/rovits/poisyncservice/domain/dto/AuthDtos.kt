package com.rovits.poisyncservice.domain.dto

import com.rovits.poisyncservice.util.MessageKeys
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

// ===== REQUEST MODELS =====

/**
 * Login request with Firebase ID token
 * Works for both email/password and social login (Google, Facebook, Apple)
 */
@Schema(description = "User login request using Firebase ID token")
data class LoginRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Schema(
        description = "Firebase ID Token received from client SDK after authentication",
        example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val firebaseToken: String
)

/**
 * Registration request with Firebase ID token
 */
@Schema(description = "New user registration request using Firebase ID token")
data class RegisterRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Schema(
        description = "Firebase ID Token received from client SDK after user creation",
        example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val firebaseToken: String
)

/**
 * Send password reset email request
 */
@Schema(description = "Request to send password reset email")
data class SendPasswordResetRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Email(message = MessageKeys.VALIDATION_EMAIL)
    @field:Schema(
        description = "Email address to send password reset link",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val email: String
)

/**
 * Send email verification request
 */
@Schema(description = "Request to send email verification")
data class SendEmailVerificationRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Schema(
        description = "Firebase ID Token of the user",
        example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val firebaseToken: String
)

/**
 * Update user role request (admin only)
 */
@Schema(description = "Request to update user role")
data class UpdateUserRoleRequest(
    @field:NotBlank(message = MessageKeys.VALIDATION_REQUIRED)
    @field:Pattern(
        regexp = "^(user|admin)$",
        message = "Role must be either 'user' or 'admin'"
    )
    @field:Schema(
        description = "New role for the user",
        example = "admin",
        allowableValues = ["user", "admin"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val role: String
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
    val name: String?,

    @field:Schema(description = "User role", example = "user", allowableValues = ["user", "admin"])
    val role: String = "user"
)

@Schema(description = "Logout request payload")
data class LogoutRequest(
    @field:Schema(description = "Refresh token to be invalidated", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String?
)