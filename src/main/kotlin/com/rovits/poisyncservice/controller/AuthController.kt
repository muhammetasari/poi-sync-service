package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.domain.dto.AuthResponse
import com.rovits.poisyncservice.domain.dto.LoginRequest
import com.rovits.poisyncservice.domain.dto.RegisterRequest
import com.rovits.poisyncservice.domain.dto.SocialLoginRequest
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.service.AuthService
import com.rovits.poisyncservice.util.ResponseHelper
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/social-login")
    fun socialLogin(
        @Valid @RequestBody request: SocialLoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Social login request: provider={}", request.provider)
        val authResponse = authService.socialLogin(request)
        logger.info("Social login successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Registration request: email={}", request.email)
        val authResponse = authService.register(request)
        logger.info("Registration successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Login request: email={}", request.email)
        val authResponse = authService.login(request)
        logger.info("Login successful: email={}", authResponse.user.email)
        return ResponseHelper.ok(authResponse)
    }
}