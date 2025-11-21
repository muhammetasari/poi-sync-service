package com.rovits.poisyncservice.constants

/**
 * Proje genelinde kullanılan merkezi varsayılan değerler.
 */
object DefaultValues {
    /** Varsayılan arama yarıçapı (metre cinsinden) */
    const val DEFAULT_RADIUS_METERS: Double = 5000.0

    /** Varsayılan mekan türü */
    const val DEFAULT_PLACE_TYPE: String = "restaurant"

    /** Varsayılan dil kodu */
    const val DEFAULT_LANGUAGE_CODE: String = "en"

    /** Varsayılan maksimum sonuç sayısı */
    const val DEFAULT_MAX_RESULTS: Int = 20

    /** Varsayılan rate limit (anonim) */
    const val DEFAULT_RATE_LIMIT_ANONYMOUS: Int = 20

    /** Varsayılan rate limit (authenticated) */
    const val DEFAULT_RATE_LIMIT_AUTHENTICATED: Int = 100

    /** Varsayılan rate limit periyodu (saniye) */
    const val DEFAULT_RATE_LIMIT_PERIOD_SECONDS: Long = 60L
}
