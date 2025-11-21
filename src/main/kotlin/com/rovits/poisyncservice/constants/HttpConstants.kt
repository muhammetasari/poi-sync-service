package com.rovits.poisyncservice.constants

/**
 * HTTP ve güvenlik ile ilgili sabit değerler.
 * HTTP header isimleri, status kodları, encoding ve token sabitleri.
 */
object HttpConstants {

    // ===================================
    // HTTP Headers
    // ===================================
    /** Authorization header adı */
    const val HEADER_AUTHORIZATION = "Authorization"

    /** Bearer token prefix */
    const val BEARER_PREFIX = "Bearer "

    /** Bearer token prefix uzunluğu (substring için) */
    const val BEARER_PREFIX_LENGTH = 7

    /** X-Forwarded-For header adı (client IP tespiti için) */
    const val HEADER_X_FORWARDED_FOR = "X-Forwarded-For"

    /** X-Correlation-ID header adı (request tracking için) */
    const val HEADER_X_CORRELATION_ID = "X-Correlation-ID"

    /** Google API Key header adı */
    const val HEADER_X_GOOG_API_KEY = "X-Goog-Api-Key"

    /** Google Field Mask header adı */
    const val HEADER_X_GOOG_FIELD_MASK = "X-Goog-FieldMask"

    // ===================================
    // HTTP Status Codes
    // ===================================
    /** Too Many Requests HTTP status code */
    const val STATUS_TOO_MANY_REQUESTS = 429

    // ===================================
    // Encoding
    // ===================================
    /** Default character encoding */
    const val ENCODING_UTF_8 = "UTF-8"

    // ===================================
    // MDC Keys
    // ===================================
    /** MDC key for correlation ID (logging) */
    const val MDC_KEY_CORRELATION_ID = "correlationId"
}

