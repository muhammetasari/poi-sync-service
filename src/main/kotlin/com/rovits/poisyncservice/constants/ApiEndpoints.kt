package com.rovits.poisyncservice.constants

/**
 * API endpoint ve path sabitleri.
 * Internal ve external API URL'lerini merkezi olarak yönetir.
 */
object ApiEndpoints {

    // ===================================
    // Internal API Paths
    // ===================================
    /** Auth API base path */
    const val API_AUTH = "/api/auth"

    /** Places API base path */
    const val API_PLACES = "/api/places"

    /** Sync API base path */
    const val API_SYNC = "/api/sync"

    /** Test API base path */
    const val API_TEST = "/api/test"

    // ===================================
    // Public Endpoints (No Auth Required)
    // ===================================
    /** Auth endpoints pattern (for security config) */
    const val PATTERN_AUTH = "/api/auth/**"

    /** Test endpoints pattern (for security config) */
    const val PATTERN_TEST = "/api/test/**"

    /** Sync endpoints pattern (for security config) */
    const val PATTERN_SYNC = "/api/sync/**"

    /** Actuator health endpoint */
    const val ACTUATOR_HEALTH = "/actuator/health"

    /** OpenAPI docs base path */
    const val OPENAPI_DOCS = "/v3/api-docs"

    /** OpenAPI docs pattern */
    const val PATTERN_OPENAPI_DOCS = "/v3/api-docs/**"

    /** Swagger UI HTML page */
    const val SWAGGER_UI_HTML = "/swagger-ui.html"

    /** Swagger UI base path */
    const val SWAGGER_UI = "/swagger-ui"

    /** Swagger UI pattern */
    const val PATTERN_SWAGGER_UI = "/swagger-ui/**"

    // ===================================
    // Google Places API
    // ===================================
    /** Google Places API base URL */
    const val GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/v1"

    /** Google Places Nearby Search endpoint */
    const val GOOGLE_PLACES_SEARCH_NEARBY = "/places:searchNearby"

    /** Google Places Text Search endpoint */
    const val GOOGLE_PLACES_SEARCH_TEXT = "/places:searchText"

    /** Google Places Details endpoint (with path variable) */
    const val GOOGLE_PLACES_DETAILS = "/places/{placeId}"
}

