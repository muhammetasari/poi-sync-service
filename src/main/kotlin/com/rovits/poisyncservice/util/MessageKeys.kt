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

    // ===================================
    // EXTERNAL & SYSTEM
    // ===================================
    const val GOOGLE_API_FAILED = "error.google.api.failed"
    const val GOOGLE_API_UNAVAILABLE = "error.google.api.unavailable"
    const val EXTERNAL_SERVICE_TIMEOUT = "error.external.service.timeout"
    const val DATABASE_FAILED = "error.database.failed"
    const val DATABASE_DUPLICATE_KEY = "error.database.duplicate.key"
    const val CACHE_OPERATION_FAILED = "error.cache.operation.failed"
    const val INTERNAL_SERVER_ERROR = "error.internal.server"
    const val TOO_MANY_REQUESTS = "error.too.many.requests"

    // ===================================
    // POI
    // ===================================
    const val POI_SYNC_FAILED = "error.poi.sync.failed"
    const val POI_UNKNOWN_NAME = "poi.unknown.name"
    const val POI_UNKNOWN_ADDRESS = "poi.unknown.address"
}