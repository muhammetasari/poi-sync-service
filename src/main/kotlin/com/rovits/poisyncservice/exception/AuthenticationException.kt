package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception for authentication failures.
 * Used when user credentials are invalid or authentication token is expired/invalid.
 *
 * Examples:
 * - Invalid email or password
 * - Expired JWT token
 * - Invalid Firebase token
 * - Missing authentication credentials
 *
 * @param errorCode Unique error code (e.g., "AUTH_001")
 * @param messageKey i18n message key (e.g., "error.invalid.credentials")
 * @param messageArgs Arguments for message placeholders
 * @param cause Original exception if any
 */
class AuthenticationException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    httpStatus = HttpStatus.UNAUTHORIZED,
    cause = cause
)