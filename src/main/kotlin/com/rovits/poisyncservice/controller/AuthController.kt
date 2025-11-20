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
@Tag(name = "Authentication", description = "Kullanıcı Kayıt, Giriş ve Çıkış İşlemleri")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @Operation(summary = "Sosyal Medya ile Giriş", description = "Google, Facebook veya Apple gibi sağlayıcılardan alınan Firebase token ile giriş yapar.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Giriş başarılı, token döndü"),
            SwaggerApiResponse(responseCode = "400", description = "Geçersiz provider veya eksik bilgi")
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

    @Operation(summary = "Kullanıcı Kaydı", description = "Email ve şifre ile yeni kullanıcı oluşturur.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Kayıt başarılı"),
            SwaggerApiResponse(responseCode = "400", description = "Validasyon hatası (örn: geçersiz email, zayıf şifre)"),
            SwaggerApiResponse(responseCode = "409", description = "Kullanıcı zaten mevcut")
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

    @Operation(summary = "Kullanıcı Girişi", description = "Email ve şifre ile sisteme giriş yapar.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Giriş başarılı"),
            SwaggerApiResponse(responseCode = "401", description = "Hatalı email veya şifre")
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

    @Operation(summary = "Çıkış Yap (Logout)", description = "Kullanıcının Access ve Refresh tokenlarını geçersiz kılar (Blacklist).")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Çıkış başarılı"),
            SwaggerApiResponse(responseCode = "401", description = "Token geçersiz veya eksik")
        ]
    )
    @PostMapping("/logout")
    fun logout(
        @Parameter(description = "Bearer Access Token", required = true)
        @RequestHeader("Authorization") authHeader: String,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refresh token (opsiyonel)", required = false)
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