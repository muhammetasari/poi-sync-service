package com.rovits.poisyncservice.util

/**
 * Centralized storage for i18n message keys.
 * Prevents "Magic String" usage across the application.
 */
object MessageKeys {
    // ===================================
    // USER & AUTH
    // ===================================
    const val USER_NOT_FOUND = "error.user.not.found"
    const val USER_ALREADY_EXISTS = "error.user.already.exists"
    const val INVALID_CREDENTIALS = "error.invalid.credentials"
    const val UNAUTHORIZED = "error.unauthorized"
    const val ACCESS_DENIED = "error.access.denied"
    const val TOKEN_INVALID = "error.token.invalid"
    const val TOKEN_EXPIRED = "error.token.expired"
    const val FIREBASE_TOKEN_INVALID = "error.firebase.token.invalid"
    const val MISSING_AUTHENTICATION = "error.missing.authentication"
    const val JWT_GENERATION_FAILED = "error.jwt.generation.failed"
    const val AUTH_EMAIL_NOT_VERIFIED = "error.auth.email.not.verified"
    const val AUTH_PROVIDER_MISMATCH = "error.auth.provider.mismatch"
    const val VALIDATION_INVALID_TOKEN = "error.validation.invalid.token"


    // ===================================
    // VALIDATION
    // ===================================
    const val VALIDATION_FAILED = "error.validation.failed"
    const val VALIDATION_REQUIRED = "error.validation.required"
    const val VALIDATION_EMAIL = "error.validation.email"
    const val VALIDATION_LATITUDE = "error.validation.latitude"
    const val VALIDATION_LONGITUDE = "error.validation.longitude"
    const val VALIDATION_RADIUS = "error.validation.radius"
    const val VALIDATION_PROVIDER = "error.validation.provider.invalid"
    const val VALIDATION_PASSWORD_STRENGTH = "error.validation.password.strength"
    const val VALIDATION_PASSWORD_MIN = "error.validation.password.min"
    const val VALIDATION_NAME_SIZE = "error.validation.name.size"
    const val PASSWORD_POLICY = "error.validation.password.policy"

    // ===================================
    // EXTERNAL & SYSTEM
    // ===================================
    const val GOOGLE_API_FAILED = "error.google.api.failed"
    const val GOOGLE_API_UNAVAILABLE = "error.google.api.unavailable"
    const val EXTERNAL_SERVICE_TIMEOUT = "error.external.service.timeout"

    // Firebase
    const val FIREBASE_FAILED = "error.firebase.failed"
    const val FIREBASE_UNAVAILABLE = "error.firebase.unavailable"

    // Database
    const val DATABASE_FAILED = "error.database.failed"
    const val DATABASE_DUPLICATE_KEY = "error.database.duplicate.key"
    const val DATABASE_UNAVAILABLE = "error.database.unavailable"
    const val DATABASE_CONNECTION_FAILED = "error.database.connection.failed"

    // Cache
    const val CACHE_OPERATION_FAILED = "error.cache.operation.failed"
    const val CACHE_UNAVAILABLE = "error.cache.unavailable"
    const val CACHE_SERIALIZATION_FAILED = "error.cache.serialization.failed"
    const val CACHE_CONNECTION_FAILED = "error.cache.connection.failed"

    // System
    const val INTERNAL_SERVER_ERROR = "error.internal.server"
    const val RATE_LIMIT_EXCEEDED = "error.rate.limit.exceeded"

    // ===================================
    // POI
    // ===================================
    const val POI_NOT_FOUND = "error.poi.not.found"
    const val POI_SYNC_FAILED = "error.poi.sync.failed"
    const val POI_UNKNOWN_NAME = "poi.unknown.name"
    const val POI_UNKNOWN_ADDRESS = "poi.unknown.address"

    // ===================================
    // VALIDATION - ADDITIONAL
    // ===================================
    const val VALIDATION_TYPE_MISMATCH = "error.validation.type.mismatch"
    const val VALIDATION_JSON_MALFORMED = "error.validation.json.malformed"
}
