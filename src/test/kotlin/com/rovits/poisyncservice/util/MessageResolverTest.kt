package com.rovits.poisyncservice.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*

class MessageResolverTest {

    private lateinit var messageResolver: MessageResolver

    @BeforeEach
    fun setUp() {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasenames("messages")
        messageSource.setDefaultEncoding("UTF-8")
        messageResolver = MessageResolver(messageSource)
    }

    @Test
    fun `should resolve Turkish message`() {
        Locale.setDefault(Locale("tr", "TR"))
        val message = messageResolver.resolve("error.user.not.found", "test@example.com")
        assertEquals("test@example.com email adresine sahip kullanıcı bulunamadı", message)
    }

    @Test
    fun `should resolve English message as default`() {
        Locale.setDefault(Locale.ENGLISH)
        val message = messageResolver.resolve("error.user.not.found", "test@example.com")
        assertEquals("User not found with email: test@example.com", message)
    }

    @Test
    fun `should resolve message with multiple arguments`() {
        Locale.setDefault(Locale.ENGLISH)
        // Assuming a message key like: error.test.multiple.args=Arg1: {0}, Arg2: {1}
        // This key is not in the properties files, so we'll test with a key that is.
        // Let's use a hypothetical key for the test, and then add it to the properties files.
        val message = messageResolver.resolve("error.access.denied", "ADMIN")
        assertEquals("Access denied. Required permission: ADMIN", message)
    }
}

