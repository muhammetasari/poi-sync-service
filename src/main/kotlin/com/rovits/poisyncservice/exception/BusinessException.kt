package com.rovits.poisyncservice.exception

/**
 * Exception for business logic violations.
 * Maps to HTTP 409 CONFLICT in GlobalExceptionHandler.
 */
class BusinessException(
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