package com.rovits.poisyncservice.util

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.util.*

/**
 * Helper class for resolving i18n messages.
 * Automatically detects user's locale from Accept-Language header.
 */
@Component
class MessageResolver(
    private val messageSource: MessageSource
) {

    /**
     * Resolves a message using the current locale from LocaleContextHolder
     *
     * @param messageKey Key from messages.properties (e.g., "error.user.not.found")
     * @param args Arguments for message placeholders {0}, {1}, etc.
     * @return Localized message
     */
    fun resolve(messageKey: String, vararg args: Any?): String {
        val locale = LocaleContextHolder.getLocale()
        return try {
            messageSource.getMessage(messageKey, args, locale)
        } catch (e: Exception) {
            // Fallback to message key if resolution fails
            messageKey
        }
    }

    /**
     * Resolves a message with a specific locale (overrides Accept-Language)
     *
     * @param messageKey Key from messages.properties
     * @param locale Specific locale to use
     * @param args Arguments for message placeholders
     * @return Localized message
     */
    fun resolve(messageKey: String, locale: Locale, vararg args: Any?): String {
        return try {
            messageSource.getMessage(messageKey, args, locale)
        } catch (e: Exception) {
            messageKey
        }
    }

    /**
     * Resolves a message with default text if key is not found
     *
     * @param messageKey Key from messages.properties
     * @param defaultMessage Default message if key not found
     * @param args Arguments for message placeholders
     * @return Localized message or default message
     */
    fun resolveOrDefault(messageKey: String, defaultMessage: String, vararg args: Any?): String {
        val locale = LocaleContextHolder.getLocale()
        return try {
            messageSource.getMessage(messageKey, args, defaultMessage, locale) ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }

    /**
     * Gets the current locale from the request context
     *
     * @return Current locale (from Accept-Language header)
     */
    fun getCurrentLocale(): Locale {
        return LocaleContextHolder.getLocale()
    }
}