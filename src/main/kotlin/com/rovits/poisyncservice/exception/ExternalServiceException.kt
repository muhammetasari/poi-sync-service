package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception for external service/API failures.
 * Used when communication with third-party services fails.
 *
 * Examples:
 * - Google Places API error
 * - Firebase authentication failure
 * - MongoDB connection timeout
 * - Redis connection error
 *
 * @param errorCode Unique error code (e.g., "EXT_001")
 * @param messageKey i18n message key (e.g., "error.external.service.unavailable")
 * @param messageArgs Arguments for message placeholders
 * @param serviceName Name of the external service that failed
 * @param cause Original exception if any
 */
class ExternalServiceException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    val serviceName: String? = null,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    httpStatus = HttpStatus.SERVICE_UNAVAILABLE,
    cause = cause
)