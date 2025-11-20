package com.rovits.poisyncservice.exception

/**
 * Exception for authorization failures.
 * Maps to HTTP 403 FORBIDDEN in GlobalExceptionHandler.
 */
class AuthorizationException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    val requiredPermission: String? = null,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    cause = cause
)