package com.rovits.poisyncservice.constants


/**
 * Güvenlik, JWT ve authentication ile ilgili sabit değerler.
 * Şifre politikaları, rate limiting ve token sabitleri.
 */
object SecurityConstants {

    // ===================================
    // JWT Claims
    // ===================================
    /** User ID claim in JWT */
    const val JWT_CLAIM_USER_ID = "userId"

    /** JWT refresh token expiration multiplier (7x access token) */
    const val JWT_REFRESH_TOKEN_MULTIPLIER = 7

    // ===================================
    // Password Policy
    // ===================================
    /** Minimum password length */
    const val MIN_PASSWORD_LENGTH = 8

    /** Password validation reason: too short */
    const val PASSWORD_REASON_TOO_SHORT = "too.short"

    /** Password validation reason: missing digit */
    const val PASSWORD_REASON_MISSING_DIGIT = "missing.digit"

    /** Password validation reason: blacklisted */
    const val PASSWORD_REASON_BLACKLISTED = "blacklisted"

    /** Blacklisted passwords */
    val PASSWORD_BLACKLIST = setOf(
        "password", "12345678", "qwerty", "letmein", "123456789",
        "admin", "welcome", "abc123", "11111111", "123123",
        "password123", "admin123", "test123", "user", "root"
    )


    // ===================================
    // Error Messages (for RateLimitService exceptions)
    // ===================================
    /** Error message for too many user attempts */
    const val ERROR_TOO_MANY_USER_ATTEMPTS = "Too many attempts for user. Try again later."

    /** Error message for too many IP attempts */
    const val ERROR_TOO_MANY_IP_ATTEMPTS = "Too many attempts from IP. Try again later."

    // ===================================
    // Default Values
    // ===================================
    /** Unknown field name for validation errors */
    const val UNKNOWN_FIELD_NAME = "unknown"

    /** Default active profile */
    const val DEFAULT_ACTIVE_PROFILE = "dev"
}

