package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.constants.SecurityConstants
import com.rovits.poisyncservice.domain.document.UserDocument
import com.rovits.poisyncservice.exception.AuthenticationException
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.util.MessageKeys
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Suppress("DEPRECATION")
@Service
class JwtService(
    @Value("\${jwt.secret-key}") private val secretKey: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(user: UserDocument): String {
        return try {
            val now = Date().toInstant()
            val expiryDate = now.plusMillis(expirationMs)

            Jwts.builder()
                .subject(user.email)
                .claim(SecurityConstants.JWT_CLAIM_USER_ID, user.id)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(key)
                .compact()
        } catch (e: Exception) {
            throw AuthenticationException(
                errorCode = ErrorCodes.JWT_GENERATION_FAILED,
                messageKey = MessageKeys.JWT_GENERATION_FAILED,
                cause = e
            )
        }
    }

    fun generateRefreshToken(user: UserDocument): String {
        return try {
            val now = Date().toInstant()
            val expiryDate = now.plusMillis(expirationMs * SecurityConstants.JWT_REFRESH_TOKEN_MULTIPLIER)

            Jwts.builder()
                .subject(user.email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(key)
                .compact()
        } catch (e: Exception) {
            throw AuthenticationException(
                errorCode = ErrorCodes.JWT_GENERATION_FAILED,
                messageKey = MessageKeys.JWT_GENERATION_FAILED,
                cause = e
            )
        }
    }

    fun getEmailFromToken(token: String): String? {
        return try {
            getClaims(token).subject
        } catch (e: ExpiredJwtException) {
            throw AuthenticationException(
                errorCode = ErrorCodes.TOKEN_EXPIRED,
                messageKey = MessageKeys.TOKEN_EXPIRED,
                cause = e
            )
        } catch (e: Exception) {
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            throw AuthenticationException(
                errorCode = ErrorCodes.TOKEN_EXPIRED,
                messageKey = MessageKeys.TOKEN_EXPIRED,
                cause = e
            )
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        try {
            return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            throw AuthenticationException(
                errorCode = ErrorCodes.TOKEN_EXPIRED,
                messageKey = MessageKeys.TOKEN_EXPIRED,
                cause = e
            )
        }
    }

    fun getExpirationDateFromToken(token: String): Date? {
        return try {
            val claims = getClaims(token)
            claims.expiration
        } catch (e: Exception) {
            null
        }
    }
}