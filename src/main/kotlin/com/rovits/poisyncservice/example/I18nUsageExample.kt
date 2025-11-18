package com.rovits.poisyncservice.example

import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ResourceNotFoundException
import com.rovits.poisyncservice.exception.BusinessException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Example service demonstrating i18n usage with custom exceptions
 */
@Service
class I18nUsageExample(
    private val messageSource: MessageSource
) {

    /**
     * Example 1: Using exception with i18n
     * The GlobalExceptionHandler will resolve the message based on Accept-Language header
     */
    fun throwUserNotFoundException(email: String) {
        throw ResourceNotFoundException(
            errorCode = ErrorCodes.USER_NOT_FOUND,
            messageKey = "error.user.not.found",
            messageArgs = arrayOf(email)
        )
        // Request with Accept-Language: en
        // Response: "User not found with email: john@example.com"

        // Request with Accept-Language: tr
        // Response: "john@example.com email adresine sahip kullanıcı bulunamadı"
    }

    /**
     * Example 2: Multiple arguments in message
     */
    fun throwAccessDeniedException(requiredPermission: String) {
        throw BusinessException(
            errorCode = ErrorCodes.ACCESS_DENIED,
            messageKey = "error.access.denied",
            messageArgs = arrayOf(requiredPermission),
            httpStatus = HttpStatus.FORBIDDEN
        )
        // EN: "Access denied. Required permission: ADMIN"
        // TR: "Erişim reddedildi. Gerekli yetki: ADMIN"
    }

    /**
     * Example 3: Manual message resolution (if needed in service layer)
     */
    fun getLocalizedMessage(messageKey: String, vararg args: Any): String {
        val locale = LocaleContextHolder.getLocale()
        return messageSource.getMessage(messageKey, args, locale)
    }

    /**
     * Example 4: Testing different locales
     */
    fun demonstrateLocales() {
        val email = "test@example.com"

        // Will use Accept-Language header from HTTP request
        val message = getLocalizedMessage("error.user.not.found", email)

        println("Localized message: $message")
        // With Accept-Language: en -> "User not found with email: test@example.com"
        // With Accept-Language: tr -> "test@example.com email adresine sahip kullanıcı bulunamadı"
    }

    /**
     * Example 5: Validation messages with field name
     */
    fun validateEmail(email: String) {
        if (!email.contains("@")) {
            throw BusinessException(
                errorCode = ErrorCodes.INVALID_EMAIL_FORMAT,
                messageKey = "error.validation.email",
                messageArgs = arrayOf(email),
                httpStatus = HttpStatus.BAD_REQUEST
            )
            // EN: "Invalid email format: testexample.com"
            // TR: "Geçersiz e-posta formatı: testexample.com"
        }
    }

    /**
     * Example 6: External service error
     */
    fun handleGoogleApiError() {
        throw BusinessException(
            errorCode = ErrorCodes.GOOGLE_API_UNAVAILABLE,
            messageKey = "error.google.api.unavailable",
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE
        )
        // EN: "Google Places API is currently unavailable"
        // TR: "Google Places API şu anda kullanılamıyor"
    }
}

/**
 * How to test different locales:
 *
 * Using curl:
 * ```bash
 * # English
 * curl -H "Accept-Language: en" http://localhost:8080/api/users/test@example.com
 *
 * # Turkish
 * curl -H "Accept-Language: tr" http://localhost:8080/api/users/test@example.com
 *
 * # English (with quality values)
 * curl -H "Accept-Language: en-US,en;q=0.9" http://localhost:8080/api/users/test@example.com
 *
 * # Turkish (with quality values)
 * curl -H "Accept-Language: tr-TR,tr;q=0.9" http://localhost:8080/api/users/test@example.com
 *
 * # Unsupported language (falls back to English)
 * curl -H "Accept-Language: fr" http://localhost:8080/api/users/test@example.com
 * ```
 *
 * Using Postman:
 * 1. Go to Headers tab
 * 2. Add header: Accept-Language
 * 3. Value: en or tr
 */