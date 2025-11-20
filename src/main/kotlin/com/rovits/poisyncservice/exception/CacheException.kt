package com.rovits.poisyncservice.exception

/**
 * Exception for cache operation failures.
 * Maps to HTTP 500 INTERNAL SERVER ERROR in GlobalExceptionHandler.
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
    cause = cause
)