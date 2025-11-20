package com.rovits.poisyncservice.exception

/**
 * Exception for validation errors (custom logic).
 * Maps to HTTP 400 BAD REQUEST in GlobalExceptionHandler.
 */
class ValidationException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    val fieldName: String? = null,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    cause = cause
)