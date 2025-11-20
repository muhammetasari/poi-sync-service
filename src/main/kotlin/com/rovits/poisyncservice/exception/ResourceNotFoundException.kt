package com.rovits.poisyncservice.exception

/**
 * Exception thrown when a requested resource is not found.
 * Used for database queries, file access, or any resource lookup that returns empty.
 * Maps to HTTP 404 NOT FOUND in GlobalExceptionHandler.
 */
class ResourceNotFoundException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    cause = cause
)