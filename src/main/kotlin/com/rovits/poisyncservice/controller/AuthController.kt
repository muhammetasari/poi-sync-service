package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.domain.dto.AuthResponse
import com.rovits.poisyncservice.domain.dto.LoginRequest
import com.rovits.poisyncservice.domain.dto.RegisterRequest
import com.rovits.poisyncservice.domain.dto.SocialLoginRequest
import com.rovits.poisyncservice.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/social-login")
    fun socialLogin(@RequestBody request: SocialLoginRequest): ResponseEntity<AuthResponse> {
        logger.info("Social login request: provider={}", request.provider)
        val authResponse = authService.socialLogin(request)
        logger.info("Social login successful: email={}", authResponse.user.email)
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.info("Registration request: email={}", request.email)
        val authResponse = authService.register(request)
        logger.info("Registration successful: email={}", authResponse.user.email)
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        logger.info("Login request: email={}", request.email)
        val authResponse = authService.login(request)
        logger.info("Login successful: email={}", authResponse.user.email)
        return ResponseEntity.ok(authResponse)
    }
}
