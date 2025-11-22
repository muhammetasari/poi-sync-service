package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.constants.HttpConstants
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.AuthService
import com.rovits.poisyncservice.util.ResponseHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for User Registration, Login, Social Auth, and Token Management.")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @Operation(
        summary = "Register User",
        description = "Registers a new user using Firebase ID token. Client must create user in Firebase first, then send the token to backend."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Registration successful, returns JWT access/refresh tokens",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Validation error or invalid Firebase token",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "User already exists",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Registration request received")
        val authResponse = authService.register(request)
        logger.info("Registration successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }

    @Operation(
        summary = "Login User",
        description = "Authenticates user using Firebase ID token. Works for both email/password and social login (Google, Facebook, Apple). Client must authenticate with Firebase first, then send the ID token."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Login successful, returns JWT access/refresh tokens",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Invalid Firebase token or email not verified",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Login request received")
        val authResponse = authService.login(request)
        logger.info("Login successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }

    @Operation(
        summary = "Send Password Reset Email",
        description = "Sends a password reset email to the specified address via Firebase."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Password reset email sent successfully",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid email format",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/send-password-reset-email")
    fun sendPasswordResetEmail(
        @Valid @RequestBody request: SendPasswordResetRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.info("Password reset email request: email={}", request.email)
        authService.sendPasswordResetEmail(request)
        logger.info("Password reset email sent: email={}", request.email)
        return ResponseHelper.ok()
    }

    @Operation(
        summary = "Send Email Verification",
        description = "Sends an email verification link to the user's email address via Firebase."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Verification email sent successfully",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Invalid Firebase token",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/send-email-verification")
    fun sendEmailVerification(
        @Valid @RequestBody request: SendEmailVerificationRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.info("Email verification request received")
        authService.sendEmailVerification(request)
        logger.info("Email verification sent successfully")
        return ResponseHelper.ok()
    }

    @Operation(
        summary = "Update User Role",
        description = "Updates a user's role. Admin only endpoint."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "User role updated successfully",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "403",
                description = "Access denied - admin role required",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun updateUserRole(
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateUserRoleRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.info("Update user role request: userId={}, newRole={}", userId, request.role)
        authService.updateUserRole(userId, request)
        logger.info("User role updated successfully: userId={}", userId)
        return ResponseHelper.ok()
    }

    @Operation(
        summary = "Logout",
        description = "Invalidates the current Access Token (and optionally Refresh Token) by adding them to a blacklist."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Logout successful",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Token invalid or missing",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/logout")
    fun logout(
        @Parameter(description = "Bearer Access Token", required = true, example = "Bearer eyJhbGci...")
        @RequestHeader(HttpConstants.HEADER_AUTHORIZATION) authHeader: String,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Optional refresh token to invalidate")
        @RequestBody(required = false) request: LogoutRequest?
    ): ResponseEntity<ApiResponse<Unit>> {
        val accessToken = if (authHeader.startsWith(HttpConstants.BEARER_PREFIX)) {
            authHeader.substring(HttpConstants.BEARER_PREFIX_LENGTH)
        } else authHeader

        logger.info("Logout request received")
        authService.logout(accessToken, request?.refreshToken)

        return ResponseHelper.ok()
    }
}