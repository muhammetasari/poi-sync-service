package com.rovits.poisyncservice.exception

/**
 * Exception for authentication failures.
 * Maps to HTTP 401 UNAUTHORIZED in GlobalExceptionHandler.
 */
open class AuthenticationException(
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