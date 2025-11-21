package com.rovits.poisyncservice.constants

import java.util.concurrent.TimeUnit

/**
 * Proje genelinde kullanılan merkezi varsayılan değerler.
 */
object DefaultValues {
    // ===================================
    // Search Defaults
    // ===================================
    /** Varsayılan arama yarıçapı (metre cinsinden) */
    const val DEFAULT_RADIUS_METERS: Double = 5000.0

    /** Varsayılan mekan türü */
    const val DEFAULT_PLACE_TYPE: String = "restaurant"

    /** Varsayılan dil kodu */
    const val DEFAULT_LANGUAGE_CODE: String = "en"

    /** Varsayılan maksimum sonuç sayısı */
    const val DEFAULT_MAX_RESULTS: Int = 20

    // ===================================
    // Rate Limiting Defaults
    // ===================================
    /** Varsayılan rate limit (anonim kullanıcılar için) */
    const val DEFAULT_RATE_LIMIT_ANONYMOUS: Int = 20

    /** Varsayılan rate limit (authenticated kullanıcılar için) */
    const val DEFAULT_RATE_LIMIT_AUTHENTICATED: Int = 100

    /** Varsayılan rate limit periyodu (saniye) */
    const val DEFAULT_RATE_LIMIT_PERIOD_SECONDS: Long = 60L

    /** Maximum login/registration attempts per user */
    const val MAX_AUTH_ATTEMPTS: Int = 5

    /** Maximum attempts per IP address */
    const val MAX_IP_ATTEMPTS: Int = 20

    /** Rate limit block duration (milliseconds) */
    val BLOCK_DURATION_MILLIS: Long = TimeUnit.MINUTES.toMillis(10)
}
