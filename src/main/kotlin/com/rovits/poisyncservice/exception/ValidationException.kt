package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception for validation errors.
 * Used when input data fails validation rules (beyond Bean Validation).
 *
 * Examples:
 * - Invalid date range
 * - Negative values where positive required
 * - Invalid format for custom fields
 *
 * @param errorCode Unique error code (e.g., "VAL_001")
 * @param messageKey i18n message key (e.g., "error.validation.invalid.range")
 * @param messageArgs Arguments for message placeholders
 * @param fieldName Optional field name that failed validation
 * @param cause Original exception if any
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
    httpStatus = HttpStatus.BAD_REQUEST,
    cause = cause
)