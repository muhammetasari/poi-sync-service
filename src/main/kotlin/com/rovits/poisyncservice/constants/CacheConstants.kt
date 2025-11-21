package com.rovits.poisyncservice.constants

import java.util.concurrent.TimeUnit

/**
 * Redis cache ve key prefix sabitleri.
 * Cache TTL değerleri ve Redis key yapılandırması.
 */
object CacheConstants {

    // ===================================
    // Redis Key Prefixes
    // ===================================
    /** Token blacklist key prefix */
    const val PREFIX_BLACKLIST = "blacklist:"

    /** API Key based rate limit key prefix */
    const val PREFIX_APIKEY = "apikey:"

    /** User based rate limit key prefix */
    const val PREFIX_USER = "user:"

    /** IP based rate limit key prefix */
    const val PREFIX_IP = "ip:"

    /** Nearby search cache key prefix */
    const val PREFIX_SEARCH_NEARBY = "search:nearby:"

    /** Text search cache key prefix */
    const val PREFIX_SEARCH_TEXT = "search:text:"

    /** Place details cache key prefix */
    const val PREFIX_DETAILS = "details:"

    // ===================================
    // Cache TTL Values
    // ===================================
    /** Default cache TTL for search results (minutes) */
    const val TTL_SEARCH_MINUTES = 10L

    /** Cache TTL for place details (hours) */
    const val TTL_DETAILS_HOURS = 24L

    /** Cache TTL time unit for search */
    val TTL_SEARCH_UNIT: TimeUnit = TimeUnit.MINUTES

    /** Cache TTL time unit for details */
    val TTL_DETAILS_UNIT: TimeUnit = TimeUnit.HOURS

    // ===================================
    // Google Places API Field Masks
    // ===================================
    /** Field mask for nearby search */
    const val FIELD_MASK_NEARBY_SEARCH = "places.id,places.displayName,places.location"

    /** Field mask for text search */
    const val FIELD_MASK_TEXT_SEARCH = "places.id,places.displayName,places.formattedAddress,places.location"

    /** Field mask for place details */
    const val FIELD_MASK_DETAILS = "id,displayName.text,formattedAddress,regularOpeningHours,location"

    // ===================================
    // Unknown Values
    // ===================================
    /** Unknown API key identifier for rate limiting */
    const val UNKNOWN_API_KEY = "unknown"
}

