package com.rovits.poisyncservice.exception

/**
 * Base exception class for all custom exceptions in the application.
 * Provides i18n support through message keys and arguments.
 *
 * @property errorCode Unique error code for identifying the error type (e.g., "USER_001")
 * @property messageKey i18n message key for localized error messages
 * @property messageArgs Arguments to be used in the localized message placeholders
 */
abstract class BaseException(
    val errorCode: String,
    val messageKey: String,
    val messageArgs: Array<Any>? = null,
    cause: Throwable? = null
) : RuntimeException(messageKey, cause) {

    override fun toString(): String {
        return "errorCode='$errorCode', messageKey='$messageKey'"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseException) return false

        if (errorCode != other.errorCode) return false
        if (messageKey != other.messageKey) return false
        if (messageArgs != null) {
            if (other.messageArgs == null) return false
            if (!messageArgs.contentEquals(other.messageArgs)) return false
        } else if (other.messageArgs != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = errorCode.hashCode()
        result = 31 * result + messageKey.hashCode()
        result = 31 * result + (messageArgs?.contentHashCode() ?: 0)
        return result
    }
}