package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.domain.dto.AuthResponse
import com.rovits.poisyncservice.domain.dto.LoginRequest
import com.rovits.poisyncservice.domain.dto.LogoutRequest
import com.rovits.poisyncservice.domain.dto.RegisterRequest
import com.rovits.poisyncservice.domain.dto.SocialLoginRequest
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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for User Registration, Login, Social Auth, and Token Management.")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @Operation(summary = "Social Login", description = "Authenticates user using a Firebase ID token. Supports Google, Facebook, and Apple providers.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Login successful, returns JWT access/refresh tokens",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid provider or validation error",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Invalid Firebase Token",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/social-login")
    fun socialLogin(
        @Valid @RequestBody request: SocialLoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Social login request: provider={}", request.provider)
        val authResponse = authService.socialLogin(request)
        logger.info("Social login successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }

    @Operation(summary = "Register User", description = "Creates a new user account with email and password.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Registration successful",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Validation error (e.g., weak password, invalid email)",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "409",
                description = "User with this email already exists",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Registration request: email={}", request.email)
        val authResponse = authService.register(request)
        logger.info("Registration successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }

    @Operation(summary = "Login User", description = "Authenticates a user with email and password.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Login request: email={}", request.email)
        val authResponse = authService.login(request)
        logger.info("Login successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
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
        @RequestHeader("Authorization") authHeader: String,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Optional refresh token to invalidate")
        @RequestBody(required = false) request: LogoutRequest?
    ): ResponseEntity<ApiResponse<Unit>> {
        val accessToken = if (authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else authHeader

        logger.info("Logout request received")
        authService.logout(accessToken, request?.refreshToken)

        return ResponseHelper.ok()
    }
}