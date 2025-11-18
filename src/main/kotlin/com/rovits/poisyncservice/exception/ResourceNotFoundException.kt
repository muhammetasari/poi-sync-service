package com.rovits.poisyncservice.exception

import org.springframework.http.HttpStatus

/**
 * Exception thrown when a requested resource is not found.
 * Used for database queries, file access, or any resource lookup that returns empty.
 *
 * Examples:
 * - User not found by email
 * - POI not found by placeId
 * - Document not found by ID
 *
 * @param errorCode Unique error code (e.g., "USER_001")
 * @param messageKey i18n message key (e.g., "error.user.not.found")
 * @param messageArgs Arguments for message placeholders (e.g., arrayOf(email))
 * @param cause Original exception if any
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
    httpStatus = HttpStatus.NOT_FOUND,
    cause = cause
)