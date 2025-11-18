package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception for business logic violations.
 * Used when business rules are not satisfied or invalid business operations are attempted.
 *
 * Examples:
 * - User already exists with the same email
 * - Insufficient balance for transaction
 * - Invalid state transition
 *
 * @param errorCode Unique error code (e.g., "USER_002")
 * @param messageKey i18n message key (e.g., "error.user.already.exists")
 * @param messageArgs Arguments for message placeholders (e.g., arrayOf(email))
 * @param httpStatus HTTP status (default: CONFLICT)
 * @param cause Original exception if any
 */
class BusinessException(
    errorCode: String,
    messageKey: String,
    messageArgs: Array<Any>? = null,
    httpStatus: HttpStatus = HttpStatus.CONFLICT,
    cause: Throwable? = null
) : BaseException(
    errorCode = errorCode,
    messageKey = messageKey,
    messageArgs = messageArgs,
    httpStatus = httpStatus,
    cause = cause
)