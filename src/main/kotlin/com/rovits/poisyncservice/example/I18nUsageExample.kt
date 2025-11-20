package com.rovits.poisyncservice.example

import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ResourceNotFoundException
import com.rovits.poisyncservice.exception.BusinessException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

/**
 * Example service demonstrating i18n usage with custom exceptions
 */
@Service
class I18nUsageExample(
    private val messageSource: MessageSource
) {

    fun throwUserNotFoundException(email: String) {
        throw ResourceNotFoundException(
            errorCode = ErrorCodes.USER_NOT_FOUND,
            messageKey = "error.user.not.found",
            messageArgs = arrayOf(email)
        )
    }

    fun throwAccessDeniedException(requiredPermission: String) {
        // httpStatus parametresi kaldırıldı
        throw BusinessException(
            errorCode = ErrorCodes.ACCESS_DENIED,
            messageKey = "error.access.denied",
            messageArgs = arrayOf(requiredPermission)
        )
    }

    // ... (Diğer metodlar aynı) ...

    fun validateEmail(email: String) {
        if (!email.contains("@")) {
            // httpStatus parametresi kaldırıldı
            throw BusinessException(
                errorCode = ErrorCodes.INVALID_EMAIL_FORMAT,
                messageKey = "error.validation.email",
                messageArgs = arrayOf(email)
            )
        }
    }

    fun handleGoogleApiError() {
        // httpStatus parametresi kaldırıldı
        throw BusinessException(
            errorCode = ErrorCodes.GOOGLE_API_UNAVAILABLE,
            messageKey = "error.google.api.unavailable"
        )
    }
}