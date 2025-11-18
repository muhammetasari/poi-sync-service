package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception for authorization failures.
 * Used when authenticated user lacks required permissions for an operation.
 *
 * Examples:
 * - User trying to access admin-only endpoints
 * - User trying to modify another user's data
 * - Missing required role or permission
 *
 * @param errorCode Unique error code (e.g., "AUTH_004")
 * @param messageKey i18n message key (e.g., "error.access.denied")
 * @param messageArgs Arguments for message placeholders
 * @param requiredPermission Optional permission that was required
 * @param cause Original exception if any
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
    httpStatus = HttpStatus.FORBIDDEN,
    cause = cause
)