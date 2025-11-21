package com.rovits.poisyncservice.exception

/**
 * Centralized error code constants for the application.
 * Error codes follow a pattern: [DOMAIN]_[NUMBER]
 *
 * Domains:
 * - USER: User management related errors
 * - AUTH: Authentication and authorization errors
 * - VAL: Validation errors
 * - EXT: External service errors
 * - DB: Database errors
 * - CACHE: Cache operation errors
 * - SYS: System/Internal errors
 * - POI: Point of Interest related errors
 */
object ErrorCodes {

    // ===================================
    // USER DOMAIN (USER_xxx)
    // ===================================
    const val USER_NOT_FOUND = "USER_001"
    const val USER_ALREADY_EXISTS = "USER_002"
    const val INVALID_CREDENTIALS = "USER_003"
    const val USER_CREATION_FAILED = "USER_004"
    const val USER_UPDATE_FAILED = "USER_005"

    // ===================================
    // AUTHENTICATION (AUTH_xxx)
    // ===================================
    const val TOKEN_EXPIRED = "AUTH_001"
    const val TOKEN_INVALID = "AUTH_002"
    const val UNAUTHORIZED = "AUTH_003"
    const val ACCESS_DENIED = "AUTH_004"
    const val FIREBASE_TOKEN_INVALID = "AUTH_005"
    const val JWT_GENERATION_FAILED = "AUTH_006"
    const val MISSING_AUTHENTICATION = "AUTH_007"



    // ===================================
    // RATE LIMITING
    // ===================================
    const val RATE_LIMIT_EXCEEDED = "AUTH_008"
    const val AUTH_EMAIL_NOT_VERIFIED = "AUTH_009"
    const val AUTH_PROVIDER_MISMATCH = "AUTH_010"
    const val VALIDATION_INVALID_TOKEN = "AUTH_011"
    const val EMAIL_ALREADY_VERIFIED = "AUTH_012"

    // ===================================
    // VALIDATION (VAL_xxx)
    // ===================================
    const val VALIDATION_FAILED = "VAL_001"
    const val INVALID_EMAIL_FORMAT = "VAL_002"
    const val INVALID_PASSWORD_FORMAT = "VAL_003"
    const val FIELD_REQUIRED = "VAL_004"
    const val INVALID_LATITUDE = "VAL_005"
    const val INVALID_LONGITUDE = "VAL_006"
    const val INVALID_RADIUS = "VAL_007"
    const val INVALID_DATE_RANGE = "VAL_008"
    const val PASSWORD_POLICY = "VAL_013"
    const val VALIDATION_PROVIDER = "VAL_012"

    // ===================================
    // EXTERNAL SERVICES (EXT_xxx)
    // ===================================
    const val GOOGLE_API_ERROR = "EXT_001"
    const val GOOGLE_API_UNAVAILABLE = "EXT_002"
    const val GOOGLE_API_RATE_LIMIT = "EXT_003"
    const val FIREBASE_ERROR = "EXT_004"
    const val FIREBASE_UNAVAILABLE = "EXT_005"
    const val EXTERNAL_SERVICE_TIMEOUT = "EXT_006"

    // ===================================
    // DATABASE (DB_xxx)
    // ===================================
    const val DATABASE_ERROR = "DB_001"
    const val DATABASE_UNAVAILABLE = "DB_002"
    const val DATABASE_CONNECTION_FAILED = "DB_003"
    const val DUPLICATE_KEY_ERROR = "DB_004"
    const val DATABASE_TIMEOUT = "DB_005"

    // ===================================
    // CACHE (CACHE_xxx)
    // ===================================
    const val CACHE_ERROR = "CACHE_001"
    const val CACHE_UNAVAILABLE = "CACHE_002"
    const val CACHE_SERIALIZATION_ERROR = "CACHE_003"
    const val CACHE_CONNECTION_FAILED = "CACHE_004"

    // ===================================
    // POI (POI_xxx)
    // ===================================
    const val POI_NOT_FOUND = "POI_001"
    const val POI_SYNC_FAILED = "POI_002"
    const val POI_INVALID_TYPE = "POI_003"
    const val POI_SEARCH_FAILED = "POI_004"

    // ===================================
    // SYSTEM (SYS_xxx)
    // ===================================
    const val INTERNAL_SERVER_ERROR = "SYS_001"
    const val SERVICE_UNAVAILABLE = "SYS_002"
    const val CONFIGURATION_ERROR = "SYS_003"
    const val UNKNOWN_ERROR = "SYS_999"
}