package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.domain.document.UserDocument
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    // application-render.properties'ten değerleri al
    @Value("\${jwt.secret-key}") private val secretKey: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {
    // Gizli anahtarı JWT kütüphanesinin anlayacağı formata çevir
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    // Normal JWT Token üretir (Kullanıcı bilgileri içerir)
    fun generateToken(user: UserDocument): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs) // 1 gün geçerli (properties'ten)

        return Jwts.builder()
            .setSubject(user.email) // Token'ın konusu (username)
            .claim("userId", user.id) // Token içine ek bilgi (payload)
            .claim("name", user.name)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key) // Gizli anahtarla imzala
            .compact()
    }

    // Refresh Token üretir (Daha uzun ömürlü, örn: 7 gün)
    fun generateRefreshToken(user: UserDocument): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs * 7) // 7 gün geçerli

        return Jwts.builder()
            .setSubject(user.email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    // --- Token Doğrulama Metotları ---

    // Token'dan email bilgisini alır
    fun getEmailFromToken(token: String): String? {
        return try {
            getClaims(token).subject
        } catch (e: Exception) {
            null
        }
    }

    // Token geçerli mi diye kontrol eder
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Token'ın içindeki tüm bilgileri (claims) alır
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}