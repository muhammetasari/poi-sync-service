package com.rovits.poisyncservice.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*

/**
 * Configuration for internationalization (i18n) support.
 *
 * Supported locales:
 * - English (en) - default
 * - Turkish (tr)
 *
 * Locale detection:
 * - Uses Accept-Language header from HTTP request
 * - Falls back to English if unsupported locale is requested
 */
@Configuration
class MessageSourceConfig {

    /**
     * Configures the message source for loading i18n messages.
     * Messages are loaded from:
     * - src/main/resources/messages.properties (English - default)
     * - src/main/resources/messages_tr.properties (Turkish)
     */
    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()

        // Set base name for message properties files
        messageSource.setBasename("classpath:messages")

        // Use UTF-8 encoding to support Turkish characters
        messageSource.setDefaultEncoding("UTF-8")

        // Set default locale to English
        messageSource.setDefaultLocale(Locale.ENGLISH)

        // Cache messages for 1 hour (3600 seconds)
        // Set to -1 in development to reload on every request
        messageSource.setCacheSeconds(3600)

        // Fallback to system locale if message not found
        messageSource.setFallbackToSystemLocale(false)

        return messageSource
    }

    /**
     * Configures locale resolution strategy.
     * Uses Accept-Language header from HTTP request to determine user's locale.
     *
     * Example requests:
     * - Accept-Language: en-US,en;q=0.9 -> English
     * - Accept-Language: tr-TR,tr;q=0.9 -> Turkish
     * - Accept-Language: fr-FR,fr;q=0.9 -> English (fallback)
     */
    @Bean
    fun localeResolver(): LocaleResolver {
        val resolver = AcceptHeaderLocaleResolver()

        // Set default locale to English
        resolver.setDefaultLocale(Locale.ENGLISH)

        // Supported locales
        resolver.supportedLocales = listOf(
            Locale.ENGLISH,
            Locale("tr")  // Turkish
        )

        return resolver
    }
}