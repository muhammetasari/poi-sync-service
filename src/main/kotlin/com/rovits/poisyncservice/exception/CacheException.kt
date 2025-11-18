package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception for cache operation failures.
 * Used when cache read/write operations fail.
 *
 * Examples:
 * - Redis connection timeout
 * - Cache serialization error
 * - Cache eviction failure
 *
 * Note: This exception typically doesn't fail the entire request.
 * The application should continue working without cache.
 *
 * @param errorCode Unique error code (e.g., "CACHE_001")
 * @param messageKey i18n message key (e.g., "error.cache.operation.failed")
 * @param messageArgs Arguments for message placeholders
 * @param operation The cache operation that failed (e.g., "GET", "PUT", "EVICT")
 * @param cause Original exception if any
 */
class CacheException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    val operation: String? = null,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    cause = cause
)