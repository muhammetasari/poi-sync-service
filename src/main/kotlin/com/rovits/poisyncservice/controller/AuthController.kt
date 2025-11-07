package com.rovits.poisyncservice.controller

import com.rovits.poisyncservice.domain.dto.AuthResponse
import com.rovits.poisyncservice.domain.dto.LoginRequest
import com.rovits.poisyncservice.domain.dto.RegisterRequest // YENİ IMPORT
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
        // ... (Bu metot Adım 6'da yazıldığı gibi kalıyor) ...
        logger.info("-> POST /auth/social-login isteği alındı (Provider: ${request.provider})")
        val authResponse = authService.socialLogin(request)
        logger.info("<- POST /auth/social-login cevabı: Giriş başarılı (Kullanıcı: ${authResponse.user.email})")
        return ResponseEntity.ok(authResponse)
    }

    // YENİ EKLENEN ENDPOINT
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.info("-> POST /auth/register isteği alındı (${request.email})")
        val authResponse = authService.register(request)
        logger.info("<- POST /auth/register cevabı: Kayıt başarılı.")
        // Android tarafı için kayıt sonrası otomatik giriş (200 OK)
        return ResponseEntity.ok(authResponse)
    }

    // GÜNCELLENEN ENDPOINT (Geçici 501 hatası kaldırıldı)
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        logger.info("-> POST /auth/login isteği alındı (${request.email})")

        // Servis metodunu çağır
        val authResponse = authService.login(request)

        logger.info("<- POST /auth/login cevabı: Giriş başarılı.")
        return ResponseEntity.ok(authResponse)
    }
}