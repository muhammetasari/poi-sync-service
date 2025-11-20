package com.rovits.poisyncservice.exception

/**
 * Exception for external service/API failures.
 * Maps to HTTP 503 SERVICE UNAVAILABLE in GlobalExceptionHandler.
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
    cause = cause
)